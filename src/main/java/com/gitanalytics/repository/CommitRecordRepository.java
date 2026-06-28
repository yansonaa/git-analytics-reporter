package com.gitanalytics.repository;

import com.gitanalytics.entity.CommitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommitRecordRepository extends JpaRepository<CommitRecord, String> {

    List<CommitRecord> findByProjectIdAndCommitTimeBetween(String projectId, LocalDateTime start, LocalDateTime end);

    List<CommitRecord> findByAuthorEmailAndCommitTimeBetween(String authorEmail, LocalDateTime start, LocalDateTime end);

    @Query("SELECT c FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime >= :since ORDER BY c.commitTime DESC")
    List<CommitRecord> findRecentByProjectId(@Param("projectId") String projectId, @Param("since") LocalDateTime since);

    @Query("SELECT c FROM CommitRecord c WHERE c.commitTime BETWEEN :start AND :end")
    List<CommitRecord> findByCommitTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime BETWEEN :start AND :end")
    long countByProjectIdAndTimeRange(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.authorEmail, COUNT(c) FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime BETWEEN :start AND :end GROUP BY c.authorEmail")
    List<Object[]> countCommitsByAuthor(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.authorEmail, SUM(c.addLines), SUM(c.deleteLines), SUM(c.netLines) FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime BETWEEN :start AND :end GROUP BY c.authorEmail")
    List<Object[]> sumLinesByAuthor(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', c.commitTime), COUNT(c) FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime BETWEEN :start AND :end GROUP BY FUNCTION('DATE', c.commitTime) ORDER BY FUNCTION('DATE', c.commitTime)")
    List<Object[]> dailyCommitTrend(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('HOUR', c.commitTime), FUNCTION('DAYOFWEEK', c.commitTime), COUNT(c) FROM CommitRecord c WHERE c.projectId = :projectId AND c.commitTime BETWEEN :start AND :end GROUP BY FUNCTION('HOUR', c.commitTime), FUNCTION('DAYOFWEEK', c.commitTime)")
    List<Object[]> hourlyDistribution(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
