package com.gitanalytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mr_id", length = 64, nullable = false)
    private String mergeRequestId;

    @Column(name = "reviewer_email", length = 255, nullable = false)
    private String reviewerEmail;

    @Column(name = "review_time", nullable = false)
    private LocalDateTime reviewTime;

    @Column(name = "comment_count")
    private int commentCount;

    @Column(name = "response_seconds")
    private long responseSeconds;

    @Column(name = "project_id", length = 64)
    private String projectId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
