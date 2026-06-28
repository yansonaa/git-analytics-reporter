package com.gitanalytics.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeatmapData {
    private String[] hourLabels;
    private String[] dayLabels;
    private int[][] values;
}
