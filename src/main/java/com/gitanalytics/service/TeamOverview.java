package com.gitanalytics.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamOverview {
    private long totalCommits;
    private int activeMembers;
    private double avgDailyCommits;
    private long totalAddLines;
    private long totalDeleteLines;
    private long totalNetLines;
}
