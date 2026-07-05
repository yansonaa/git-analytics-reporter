package com.gitanalytics.controller;

import com.gitanalytics.entity.CommitRecord;
import com.gitanalytics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService exportService;
    private final com.gitanalytics.repository.CommitRecordRepository commitRecordRepository;

    @GetMapping("/team")
    public ResponseEntity<ReportExportService.ReportData> getTeamReport(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.generateTeamReport(projectId, start, end));
    }

    @GetMapping("/team/export")
    public ResponseEntity<byte[]> exportTeamReport(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws IOException {

        var data = reportService.generateTeamReport(projectId, start, end);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportService.exportToExcel(data, baos);

        byte[] bytes = baos.toByteArray();
        String filename = "GitAnalytics_" + projectId + "_" + start.toLocalDate() + "_" + end.toLocalDate() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /**
     * 导出原始 git log 数据（commit 记录）为 CSV 或 Excel
     *
     * @param projectId 项目ID
     * @param start     开始时间
     * @param end       结束时间
     * @param format    导出格式: csv 或 excel（默认 csv）
     */
    @GetMapping("/commits/export")
    public ResponseEntity<byte[]> exportRawCommits(
            @RequestParam String projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "csv") String format) throws IOException {

        List<CommitRecord> commits = commitRecordRepository.findByProjectIdAndCommitTimeBetween(projectId, start, end);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String extension;
        MediaType mediaType;
        if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
            exportService.exportRawCommitsToExcel(commits, baos);
            extension = "xlsx";
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            exportService.exportRawCommitsToCsv(commits, baos);
            extension = "csv";
            mediaType = MediaType.parseMediaType("text/csv;charset=UTF-8");
        }

        String filename = "GitLog_" + projectId + "_" + start.toLocalDate() + "_" + end.toLocalDate() + "." + extension;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(baos.toByteArray());
    }
}
