package com.gitanalytics.repository;

import com.gitanalytics.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    List<ReviewRecord> findByProjectIdAndReviewTimeBetween(String projectId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT r.reviewerEmail, SUM(r.commentCount), AVG(r.responseSeconds) FROM ReviewRecord r WHERE r.projectId = :projectId AND r.reviewTime BETWEEN :start AND :end GROUP BY r.reviewerEmail")
    List<Object[]> reviewStatsByReviewer(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(r.responseSeconds) FROM ReviewRecord r WHERE r.projectId = :projectId AND r.reviewTime BETWEEN :start AND :end")
    Double avgResponseTime(@Param("projectId") String projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
