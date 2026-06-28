package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsEngineTest {

    private final StatisticsEngine engine = new StatisticsEngine();

    @Test
    void testCalculateDailyCommitTrend() {
        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").commitTime(LocalDateTime.of(2026, 6, 1, 10, 0)).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("a@x.com").commitTime(LocalDateTime.of(2026, 6, 1, 14, 0)).build();
        CommitRecord c3 = CommitRecord.builder().commitId("a3").authorEmail("b@x.com").commitTime(LocalDateTime.of(2026, 6, 2, 9, 0)).build();

        Map<String, Long> trend = engine.calculateDailyCommitTrend(Arrays.asList(c1, c2, c3));
        assertEquals(2L, trend.get("2026-06-01"));
        assertEquals(1L, trend.get("2026-06-02"));
    }

    @Test
    void testCalculateHeatmap() {
        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").commitTime(LocalDateTime.of(2026, 6, 1, 10, 0)).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("a@x.com").commitTime(LocalDateTime.of(2026, 6, 2, 10, 0)).build();
        CommitRecord c3 = CommitRecord.builder().commitId("a3").authorEmail("b@x.com").commitTime(LocalDateTime.of(2026, 6, 1, 14, 0)).build();

        HeatmapData heatmap = engine.calculateHeatmap(Arrays.asList(c1, c2, c3));
        assertNotNull(heatmap);
        assertEquals(7, heatmap.getDayLabels().length);
        assertEquals(24, heatmap.getHourLabels().length);
        assertEquals(7, heatmap.getValues().length);
        assertEquals(24, heatmap.getValues()[0].length);
        // Monday 10:00 -> 2
        assertEquals(2, heatmap.getValues()[0][10]);
    }

    @Test
    void testCalculateTeamOverview() {
        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").addLines(100).deleteLines(20).netLines(80).commitTime(LocalDateTime.of(2026, 6, 1, 10, 0)).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("b@x.com").addLines(50).deleteLines(10).netLines(40).commitTime(LocalDateTime.of(2026, 6, 1, 14, 0)).build();

        TeamOverview ov = engine.calculateTeamOverview(Arrays.asList(c1, c2), 2);
        assertEquals(2L, ov.getTotalCommits());
        assertEquals(2, ov.getActiveMembers());
        assertEquals(150, ov.getTotalAddLines());
        assertEquals(30, ov.getTotalDeleteLines());
        assertEquals(120, ov.getTotalNetLines());
    }

    @Test
    void testCalculateCodeMetricsByAuthor() {
        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").addLines(100).deleteLines(20).netLines(80).commitTime(LocalDateTime.now()).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("a@x.com").addLines(50).deleteLines(10).netLines(40).commitTime(LocalDateTime.now()).build();

        Map<String, StatisticsEngine.CodeMetrics> metrics = engine.calculateCodeMetricsByAuthor(Arrays.asList(c1, c2));
        assertEquals(2L, metrics.get("a@x.com").getCommits());
        assertEquals(150L, metrics.get("a@x.com").getAddLines());
        assertEquals(120L, metrics.get("a@x.com").getNetLines());
    }
}
