package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import com.gitanalytics.entity.ReviewRecord;
import com.gitanalytics.entity.TeamMember;
import com.gitanalytics.repository.CommitRecordRepository;
import com.gitanalytics.repository.ReviewRecordRepository;
import com.gitanalytics.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final CommitRecordRepository commitRecordRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StatisticsEngine statisticsEngine;
    private final AnomalyDetector anomalyDetector;

    @Cacheable(value = "teamReport", key = "#projectId + ':' + #start + ':' + #end")
    public ReportExportService.ReportData generateTeamReport(String projectId, LocalDateTime start, LocalDateTime end) {
        log.info("生成团队报表: projectId={}, {} to {}", projectId, start, end);

        List<CommitRecord> commits = commitRecordRepository.findByProjectIdAndCommitTimeBetween(projectId, start, end);
        List<ReviewRecord> reviews = reviewRecordRepository.findByProjectIdAndReviewTimeBetween(projectId, start, end);
        List<TeamMember> members = teamMemberRepository.findAll(); // 可优化为按 teamId 过滤

        int activeMembers = (int) commits.stream().map(CommitRecord::getAuthorEmail).distinct().count();

        TeamOverview overview = statisticsEngine.calculateTeamOverview(commits, activeMembers);

        Map<String, Long> reviewComments = reviews.stream()
                .collect(Collectors.groupingBy(ReviewRecord::getReviewerEmail, Collectors.summingLong(ReviewRecord::getCommentCount)));

        Map<String, Double> responseRates = new HashMap<>();
        List<Object[]> reviewStats = reviewRecordRepository.reviewStatsByReviewer(projectId, start, end);
        for (Object[] stat : reviewStats) {
            String email = (String) stat[0];
            Double avgResp = stat[2] != null ? ((Number) stat[2]).doubleValue() : 0;
            double rate = avgResp > 0 ? Math.min(1.0, 86400.0 / avgResp) : 0; // 简化响应率
            responseRates.put(email, rate);
        }

        Map<String, Double> activityScores = statisticsEngine.calculateActivityScore(commits, reviewComments, responseRates);

        Map<String, StatisticsEngine.CodeMetrics> metrics = statisticsEngine.calculateCodeMetricsByAuthor(commits);
        List<ReportExportService.MemberRanking> ranking = new ArrayList<>();

        for (Map.Entry<String, StatisticsEngine.CodeMetrics> entry : metrics.entrySet()) {
            String email = entry.getKey();
            StatisticsEngine.CodeMetrics m = entry.getValue();
            TeamMember member = members.stream().filter(tm -> tm.getEmail().equals(email)).findFirst().orElse(null);
            String name = member != null ? member.getName() : email;

            ranking.add(ReportExportService.MemberRanking.builder()
                    .name(name)
                    .email(email)
                    .commits(m.getCommits())
                    .netLines(m.getNetLines())
                    .reviewComments(reviewComments.getOrDefault(email, 0L))
                    .activityScore(activityScores.getOrDefault(email, 0.0))
                    .build());
        }

        ranking.sort((a, b) -> Long.compare(b.getCommits(), a.getCommits()));

        Map<String, Long> trend = statisticsEngine.calculateDailyCommitTrend(commits);
        List<String> dates = new ArrayList<>(trend.keySet());
        List<Long> values = dates.stream().map(trend::get).collect(Collectors.toList());

        HeatmapData heatmap = statisticsEngine.calculateHeatmap(commits);
        List<AnomalyResult> anomalies = anomalyDetector.detectSuspiciousCommits(commits);
        List<ReportExportService.AnomalyItem> anomalyItems = anomalies.stream().map(a ->
                ReportExportService.AnomalyItem.builder()
                        .commitId(a.getCommitId())
                        .author(a.getAuthor())
                        .time(a.getTime().toString())
                        .reason(a.getReason())
                        .build()
        ).collect(Collectors.toList());

        return ReportExportService.ReportData.builder()
                .teamOverview(overview)
                .memberRanking(ranking)
                .trendData(ReportExportService.TrendData.builder().dates(dates).values(values).build())
                .heatmapData(heatmap)
                .anomalyList(anomalyItems)
                .build();
    }
}
