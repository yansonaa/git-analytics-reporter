package com.gitanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GitAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitAnalyticsApplication.class, args);
    }
}
