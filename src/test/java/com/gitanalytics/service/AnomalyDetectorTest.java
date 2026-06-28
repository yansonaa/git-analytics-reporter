package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectorTest {

    @InjectMocks
    private AnomalyDetector detector;

    @Test
    void testDetectSuspiciousCommits_midnight() {
        ReflectionTestUtils.setField(detector, "zScoreThreshold", 3.0);
        ReflectionTestUtils.setField(detector, "midnightStart", 0);
        ReflectionTestUtils.setField(detector, "midnightEnd", 5);

        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 3, 15)).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 10, 0)).build();
        CommitRecord c3 = CommitRecord.builder().commitId("a3").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 14, 0)).build();

        List<AnomalyResult> anomalies = detector.detectSuspiciousCommits(Arrays.asList(c1, c2, c3));
        assertFalse(anomalies.isEmpty());
        assertTrue(anomalies.stream().anyMatch(a -> a.getCommitId().equals("a1") && a.getReason().contains("凌晨")));
    }

    @Test
    void testDetectSuspiciousCommits_interval() {
        ReflectionTestUtils.setField(detector, "zScoreThreshold", 1.5);
        ReflectionTestUtils.setField(detector, "midnightStart", 0);
        ReflectionTestUtils.setField(detector, "midnightEnd", 5);

        CommitRecord c1 = CommitRecord.builder().commitId("a1").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 9, 0)).build();
        CommitRecord c2 = CommitRecord.builder().commitId("a2").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 10, 0)).build();
        CommitRecord c3 = CommitRecord.builder().commitId("a3").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 11, 0)).build();
        CommitRecord c4 = CommitRecord.builder().commitId("a4").authorEmail("a@x.com").authorName("Alice")
                .commitTime(LocalDateTime.of(2026, 6, 20, 20, 0)).build(); // 9小时间隔异常

        List<AnomalyResult> anomalies = detector.detectSuspiciousCommits(Arrays.asList(c1, c2, c3, c4));
        assertFalse(anomalies.isEmpty());
        assertTrue(anomalies.stream().anyMatch(a -> a.getCommitId().equals("a4") && a.getReason().contains("间隔异常")));
    }

    @Test
    void testDetectSuspiciousCommits_empty() {
        assertTrue(detector.detectSuspiciousCommits(null).isEmpty());
        assertTrue(detector.detectSuspiciousCommits(Arrays.asList()).isEmpty());
    }
}
