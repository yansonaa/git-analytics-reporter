package com.gitanalytics.service;

import com.gitanalytics.entity.CommitRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 导出团队报表为 Excel
     */
    public void exportToExcel(ReportData data, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // Sheet 1: 概览
            createOverviewSheet(wb, data);
            // Sheet 2: 成员排名
            createMemberSheet(wb, data);
            // Sheet 3: 异常提交
            createAnomalySheet(wb, data);
            wb.write(out);
        }
    }

    private void createOverviewSheet(XSSFWorkbook wb, ReportData data) {
        Sheet sheet = wb.createSheet("团队概览");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("指标");
        header.createCell(1).setCellValue("数值");

        TeamOverview ov = data.getTeamOverview();
        int r = 1;
        createRow(sheet, r++, "总提交数", ov.getTotalCommits());
        createRow(sheet, r++, "活跃成员数", ov.getActiveMembers());
        createRow(sheet, r++, "日均提交", ov.getAvgDailyCommits());
        createRow(sheet, r++, "新增代码行", ov.getTotalAddLines());
        createRow(sheet, r++, "删除代码行", ov.getTotalDeleteLines());
        createRow(sheet, r++, "净增代码行", ov.getTotalNetLines());

        sheet.setColumnWidth(0, 20);
        sheet.setColumnWidth(1, 15);
    }

    private void createMemberSheet(XSSFWorkbook wb, ReportData data) {
        Sheet sheet = wb.createSheet("成员排名");

        // 计算均值用于条件格式
        double avgCommits = data.getMemberRanking().stream()
                .mapToDouble(MemberRanking::getCommits).average().orElse(0);
        double avgNetLines = data.getMemberRanking().stream()
                .mapToDouble(MemberRanking::getNetLines).average().orElse(0);

        XSSFCellStyle greenStyle = wb.createCellStyle();
        greenStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 198, (byte) 239, (byte) 206}, null));
        greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle normalStyle = wb.createCellStyle();
        normalStyle.setFillPattern(FillPatternType.NO_FILL);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("排名");
        header.createCell(1).setCellValue("姓名");
        header.createCell(2).setCellValue("提交数");
        header.createCell(3).setCellValue("净增行数");
        header.createCell(4).setCellValue("评审评论数");
        header.createCell(5).setCellValue("活跃度评分");

        int rank = 1;
        for (MemberRanking m : data.getMemberRanking()) {
            Row row = sheet.createRow(rank);
            row.createCell(0).setCellValue(rank);
            row.createCell(1).setCellValue(m.getName());

            Cell c2 = row.createCell(2);
            c2.setCellValue(m.getCommits());
            c2.setCellStyle(m.getCommits() > avgCommits ? greenStyle : normalStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(m.getNetLines());
            c3.setCellStyle(m.getNetLines() > avgNetLines ? greenStyle : normalStyle);

            row.createCell(4).setCellValue(m.getReviewComments());
            row.createCell(5).setCellValue(m.getActivityScore());
            rank++;
        }

        for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);
    }

    private void createAnomalySheet(XSSFWorkbook wb, ReportData data) {
        Sheet sheet = wb.createSheet("异常提交");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("提交ID");
        header.createCell(1).setCellValue("作者");
        header.createCell(2).setCellValue("时间");
        header.createCell(3).setCellValue("原因");

        int r = 1;
        for (AnomalyItem a : data.getAnomalyList()) {
            Row row = sheet.createRow(r++);
            row.createCell(0).setCellValue(a.getCommitId());
            row.createCell(1).setCellValue(a.getAuthor());
            row.createCell(2).setCellValue(a.getTime());
            row.createCell(3).setCellValue(a.getReason());
        }

        for (int i = 0; i <= 3; i++) sheet.autoSizeColumn(i);
    }

    private void createRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            row.createCell(1).setCellValue(String.valueOf(value));
        }
    }

    // DTOs
    @lombok.Data
    @lombok.Builder
    public static class ReportData {
        private TeamOverview teamOverview;
        private List<MemberRanking> memberRanking;
        private TrendData trendData;
        private HeatmapData heatmapData;
        private List<AnomalyItem> anomalyList;
    }

    @lombok.Data
    @lombok.Builder
    public static class MemberRanking {
        private String name;
        private String email;
        private long commits;
        private long netLines;
        private long reviewComments;
        private double activityScore;
    }

    @lombok.Data
    @lombok.Builder
    public static class TrendData {
        private List<String> dates;
        private List<Long> values;
    }

    @lombok.Data
    @lombok.Builder
    public static class AnomalyItem {
        private String commitId;
        private String author;
        private String time;
        private String reason;
    }

    // ========== 原始 Commit 记录导出 ==========

    /**
     * 导出原始 commit 记录为 CSV
     */
    public void exportRawCommitsToCsv(List<CommitRecord> commits, OutputStream out) throws IOException {
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8)) {
            // BOM for Excel to recognize UTF-8
            writer.write('﻿');
            // Header
            writer.write("Commit ID,Project ID,Author Name,Author Email,Commit Time,Add Lines,Delete Lines,Net Lines,File Count,Message,Merge Commit,Automated,Anomaly\n");

            for (CommitRecord c : commits) {
                writer.write(String.format("%s,%s,%s,%s,%s,%d,%d,%d,%d,\"%s\",%b,%b,%b\n",
                        c.getCommitId(),
                        escapeCsv(c.getProjectId()),
                        escapeCsv(c.getAuthorName()),
                        escapeCsv(c.getAuthorEmail()),
                        c.getCommitTime() != null ? c.getCommitTime().format(DATE_FMT) : "",
                        c.getAddLines(),
                        c.getDeleteLines(),
                        c.getNetLines(),
                        c.getFileCount(),
                        escapeCsv(c.getMessage()),
                        c.isMergeCommit(),
                        c.isAutomated(),
                        c.isAnomaly()
                ));
            }
            writer.flush();
        }
    }

    /**
     * 导出原始 commit 记录为 Excel（原始 git log 数据）
     */
    public void exportRawCommitsToExcel(List<CommitRecord> commits, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("原始提交记录");
            Row header = sheet.createRow(0);
            String[] headers = {"Commit ID", "Project ID", "Author Name", "Author Email", "Commit Time",
                    "Add Lines", "Delete Lines", "Net Lines", "File Count", "Message", "Merge Commit", "Automated", "Anomaly"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (CommitRecord c : commits) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getCommitId());
                row.createCell(1).setCellValue(c.getProjectId());
                row.createCell(2).setCellValue(c.getAuthorName());
                row.createCell(3).setCellValue(c.getAuthorEmail());
                row.createCell(4).setCellValue(c.getCommitTime() != null ? c.getCommitTime().format(DATE_FMT) : "");
                row.createCell(5).setCellValue(c.getAddLines());
                row.createCell(6).setCellValue(c.getDeleteLines());
                row.createCell(7).setCellValue(c.getNetLines());
                row.createCell(8).setCellValue(c.getFileCount());
                row.createCell(9).setCellValue(c.getMessage());
                row.createCell(10).setCellValue(c.isMergeCommit());
                row.createCell(11).setCellValue(c.isAutomated());
                row.createCell(12).setCellValue(c.isAnomaly());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"").replace("\n", " ");
    }
}
