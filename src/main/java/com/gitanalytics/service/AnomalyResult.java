package com.gitanalytics.service;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnomalyResult {
    private String commitId;
    private String author;
    private String authorEmail;
    private LocalDateTime time;
    private String reason;
}
