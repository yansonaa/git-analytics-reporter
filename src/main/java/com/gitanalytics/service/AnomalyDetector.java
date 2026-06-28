package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyDetector {

    @Value("${app.anomaly.z-score-threshold:3.0}")
    private double zScoreThreshold;

    @Value("${app.anomaly.midnight-hour-start:0}")
    private int midnightStart;

    @Value("${app.anomaly.midnight-hour-end:5}")
    private int midnightEnd;

    /**
     * 基于 Z-score 识别异常提交
     */
    public List<AnomalyResult> detectSuspiciousCommits(List<CommitRecord> commits) {
        if (commits == null || commits.size() < 3) {
            return Collections.emptyList();
        }

        List<AnomalyResult> anomalies = new ArrayList<>();
        Map<String, List<CommitRecord>> byAuthor = commits.stream()
                .collect(Collectors.groupingBy(CommitRecord::getAuthorEmail));

        for (Map.Entry<String, List<CommitRecord>> entry : byAuthor.entrySet()) {
            List<CommitRecord> authorCommits = entry.getValue();
            if (authorCommits.size() < 3) continue;

            // 按时间排序
            authorCommits.sort((a, b) -> a.getCommitTime().compareTo(b.getCommitTime()));

            // 计算提交间隔（秒）
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < authorCommits.size(); i++) {
                long seconds = ChronoUnit.SECONDS.between(
                        authorCommits.get(i - 1).getCommitTime(),
                        authorCommits.get(i).getCommitTime());
                if (seconds > 0) {
                    intervals.add(seconds);
                }
            }

            if (intervals.size() < 2) continue;

            double mean = intervals.stream().mapToDouble(Long::doubleValue).average().orElse(0);
            double variance = intervals.stream()
                    .mapToDouble(i -> Math.pow(i - mean, 2))
                    .average()
                    .orElse(0);
            double stdDev = Math.sqrt(variance);

            if (stdDev == 0) continue;

            // 检测异常间隔
            for (int i = 1; i < authorCommits.size(); i++) {
                long interval = ChronoUnit.SECONDS.between(
                        authorCommits.get(i - 1).getCommitTime(),
                        authorCommits.get(i).getCommitTime());
                double zScore = (interval - mean) / stdDev;

                if (Math.abs(zScore) > zScoreThreshold) {
                    anomalies.add(AnomalyResult.builder()
                            .commitId(authorCommits.get(i).getCommitId())
                            .author(authorCommits.get(i).getAuthorName())
                            .authorEmail(authorCommits.get(i).getAuthorEmail())
                            .time(authorCommits.get(i).getCommitTime())
                            .reason("提交间隔异常: " + formatInterval(interval) + " (Z-score: " + String.format("%.2f", zScore) + ")")
                            .build());
                }
            }
        }

        // 检测凌晨提交（批量自动化提交）
        for (CommitRecord c : commits) {
            int hour = c.getCommitTime().getHour();
            if (hour >= midnightStart && hour <= midnightEnd) {
                // 避免重复添加
                boolean alreadyAdded = anomalies.stream()
                        .anyMatch(a -> a.getCommitId().equals(c.getCommitId()));
                if (!alreadyAdded) {
                    anomalies.add(AnomalyResult.builder()
                            .commitId(c.getCommitId())
                            .author(c.getAuthorName())
                            .authorEmail(c.getAuthorEmail())
                            .time(c.getCommitTime())
                            .reason("凌晨提交 (" + hour + ":00)")
                            .build());
                }
            }
        }

        return anomalies;
    }

    private String formatInterval(long seconds) {
        if (seconds < 60) return seconds + "秒";
        if (seconds < 3600) return (seconds / 60) + "分钟";
        if (seconds < 86400) return (seconds / 3600) + "小时";
        return (seconds / 86400) + "天";
    }
}
