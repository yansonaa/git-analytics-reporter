package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsEngine {

    /**
     * 计算提交频率趋势（按日分组）
     */
    public Map<String, Long> calculateDailyCommitTrend(List<CommitRecord> commits) {
        return commits.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCommitTime().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    /**
     * 计算周度提交趋势
     */
    public Map<String, Long> calculateWeeklyCommitTrend(List<CommitRecord> commits) {
        return commits.stream()
                .collect(Collectors.groupingBy(
                        c -> {
                            LocalDateTime t = c.getCommitTime();
                            return t.getYear() + "-W" + t.toLocalDate().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                        },
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    /**
     * 计算代码量统计（按作者分组）
     */
    public Map<String, CodeMetrics> calculateCodeMetricsByAuthor(List<CommitRecord> commits) {
        Map<String, CodeMetrics> result = new HashMap<>();
        for (CommitRecord c : commits) {
            result.computeIfAbsent(c.getAuthorEmail(), k -> new CodeMetrics()).accumulate(c);
        }
        return result;
    }

    /**
     * 生成 7×24 热力图数据（星期×小时）
     */
    public HeatmapData calculateHeatmap(List<CommitRecord> commits) {
        String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        String[] hourLabels = new String[24];
        for (int i = 0; i < 24; i++) {
            hourLabels[i] = String.valueOf(i);
        }

        int[][] values = new int[7][24];
        for (CommitRecord c : commits) {
            LocalDateTime t = c.getCommitTime();
            int dayOfWeek = t.getDayOfWeek().getValue() - 1; // Monday=0
            int hour = t.getHour();
            values[dayOfWeek][hour]++;
        }

        return HeatmapData.builder()
                .dayLabels(dayLabels)
                .hourLabels(hourLabels)
                .values(values)
                .build();
    }

    /**
     * 计算活跃度评分（综合指标）
     */
    public Map<String, Double> calculateActivityScore(
            List<CommitRecord> commits,
            Map<String, Long> reviewComments,
            Map<String, Double> responseRates) {

        Map<String, Double> scores = new HashMap<>();
        Map<String, List<CommitRecord>> byAuthor = commits.stream()
                .collect(Collectors.groupingBy(CommitRecord::getAuthorEmail));

        for (Map.Entry<String, List<CommitRecord>> entry : byAuthor.entrySet()) {
            String email = entry.getKey();
            List<CommitRecord> authorCommits = entry.getValue();
            long commitCount = authorCommits.size();
            long netLines = authorCommits.stream().mapToLong(CommitRecord::getNetLines).sum();
            long comments = reviewComments.getOrDefault(email, 0L);
            double responseRate = responseRates.getOrDefault(email, 0.0);

            // 归一化（简化版，实际应基于历史均值做标准化）
            double commitScore = Math.min(commitCount / 10.0, 1.0);
            double linesScore = Math.min(netLines / 1000.0, 1.0);
            double commentScore = Math.min(comments / 20.0, 1.0);
            double responseScore = Math.min(responseRate, 1.0);

            double total = commitScore * 0.3 + linesScore * 0.2 + commentScore * 0.3 + responseScore * 0.2;
            scores.put(email, Math.round(total * 100.0) / 100.0);
        }

        return scores;
    }

    /**
     * 计算团队概览指标
     */
    public TeamOverview calculateTeamOverview(List<CommitRecord> commits, int activeMembers) {
        long totalCommits = commits.size();
        long uniqueDays = commits.stream()
                .map(c -> c.getCommitTime().toLocalDate())
                .distinct()
                .count();
        double avgDailyCommits = uniqueDays > 0 ? (double) totalCommits / uniqueDays : 0;

        long totalAddLines = commits.stream().mapToLong(CommitRecord::getAddLines).sum();
        long totalDeleteLines = commits.stream().mapToLong(CommitRecord::getDeleteLines).sum();
        long totalNetLines = commits.stream().mapToLong(CommitRecord::getNetLines).sum();

        return TeamOverview.builder()
                .totalCommits(totalCommits)
                .activeMembers(activeMembers)
                .avgDailyCommits(Math.round(avgDailyCommits * 100.0) / 100.0)
                .totalAddLines(totalAddLines)
                .totalDeleteLines(totalDeleteLines)
                .totalNetLines(totalNetLines)
                .build();
    }

    public static class CodeMetrics {
        private long commits;
        private long addLines;
        private long deleteLines;
        private long netLines;

        public void accumulate(CommitRecord c) {
            commits++;
            addLines += c.getAddLines();
            deleteLines += c.getDeleteLines();
            netLines += c.getNetLines();
        }

        public long getCommits() { return commits; }
        public long getAddLines() { return addLines; }
        public long getDeleteLines() { return deleteLines; }
        public long getNetLines() { return netLines; }
    }
}
