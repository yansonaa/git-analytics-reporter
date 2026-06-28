package com.gitanalytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "commit_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitRecord {
    @Id
    @Column(name = "commit_id", length = 40)
    private String commitId;

    @Column(name = "project_id", length = 64, nullable = false)
    private String projectId;

    @Column(name = "author_email", length = 255, nullable = false)
    private String authorEmail;

    @Column(name = "author_name", length = 100)
    private String authorName;

    @Column(name = "commit_time", nullable = false)
    private LocalDateTime commitTime;

    @Column(name = "add_lines")
    private int addLines;

    @Column(name = "delete_lines")
    private int deleteLines;

    @Column(name = "net_lines")
    private int netLines;

    @Column(name = "file_count")
    private int fileCount;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "is_merge_commit")
    private boolean mergeCommit;

    @Column(name = "is_automated")
    private boolean automated;

    @Column(name = "is_anomaly")
    private boolean anomaly;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        netLines = addLines - deleteLines;
    }
}
