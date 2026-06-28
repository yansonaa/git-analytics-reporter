package com.gitanalytics.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp = LocalDateTime.now();
}
