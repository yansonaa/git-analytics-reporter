package com.gitanalytics.controller;

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

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService exportService;

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
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
