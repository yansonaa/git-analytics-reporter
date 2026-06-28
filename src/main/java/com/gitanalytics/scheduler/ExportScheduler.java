package com.gitanalytics.scheduler;

import com.gitanalytics.service.ReportExportService;
import com.gitanalytics.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportScheduler {

    private final ReportService reportService;
    private final ReportExportService exportService;
    private final JavaMailSender mailSender;

    @Value("${app.report.from-email:reports@example.com}")
    private String fromEmail;

    @Value("${app.report.to-emails:}")
    private String toEmails;

    @Value("${app.report.default-project-id:default}")
    private String defaultProjectId;

    /**
     * 每月1号9:00执行，发送上月报表
     */
    @Scheduled(cron = "${app.report.cron:0 0 9 1 * ?}")
    public void sendMonthlyReport() {
        log.info("开始执行月度报表定时任务");

        try {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            LocalDateTime start = lastMonth.atDay(1).atStartOfDay();
            LocalDateTime end = lastMonth.atEndOfMonth().atTime(23, 59, 59);
            String monthLabel = lastMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"));

            var reportData = reportService.generateTeamReport(defaultProjectId, start, end);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            exportService.exportToExcel(reportData, baos);
            byte[] excelBytes = baos.toByteArray();

            if (toEmails == null || toEmails.isBlank()) {
                log.warn("未配置收件人邮箱，跳过邮件发送");
                return;
            }

            var mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmails.split(","));
            helper.setSubject("代码提交统计报表 - " + monthLabel);
            helper.setText("<p>请查收附件中的月度代码提交统计报表。</p>", true);

            helper.addAttachment("GitAnalytics_" + lastMonth + ".xlsx", () ->
                    new java.io.ByteArrayInputStream(excelBytes), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            mailSender.send(mimeMessage);
            log.info("月度报表邮件发送成功");
        } catch (Exception e) {
            log.error("月度报表定时任务失败", e);
        }
    }
}
