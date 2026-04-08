package com.back.domain.report.controller;

import com.back.domain.report.dto.ReportResponse;
import com.back.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    // ───────────────────────────────────────
    // 리포트 화면
    // ───────────────────────────────────────
    @GetMapping
    public String report(Model model) {
        ReportResponse report = reportService.getReport();
        model.addAttribute("report", report);
        return "report";
    }

    // ───────────────────────────────────────
    // 엑셀 다운로드
    // ───────────────────────────────────────
    @GetMapping("/download")
    public ResponseEntity<byte[]> download() {
        byte[] excelBytes = reportService.generateExcel();

        String filename = "납기리포트_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
