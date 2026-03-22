package com.back.domain.report.service;

import com.back.domain.order.order.dto.response.OrderResponse;
import com.back.domain.order.order.service.OrderService;
import com.back.domain.report.dto.ReportResponse;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderService orderService;

    // ───────────────────────────────────────
    // 리포트 데이터 조회
    // ───────────────────────────────────────
    public ReportResponse getReport() {
        List<OrderResponse> allOrders = orderService.findAll();
        return ReportResponse.of(allOrders);
    }

    // ───────────────────────────────────────
    // 엑셀 리포트 생성 (byte[])
    // ───────────────────────────────────────
    public byte[] generateExcel() {
        ReportResponse report = getReport();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── 스타일 정의 ──
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle delayStyle = createColorStyle(workbook, IndexedColors.ROSE);
            CellStyle warningStyle = createColorStyle(workbook, IndexedColors.LIGHT_YELLOW);
            CellStyle normalStyle = createColorStyle(workbook, IndexedColors.LIGHT_GREEN);

            // ── 시트 1: KPI 요약 ──
            Sheet summary = workbook.createSheet("납기 현황 요약");
            writeSummarySheet(summary, report, titleStyle, headerStyle);

            // ── 시트 2: 지연 발주 ──
            Sheet delaySheet = workbook.createSheet("지연 발주");
            writeOrderSheet(delaySheet, report.getDelayedOrders(), headerStyle, delayStyle, "🔴 지연 발주 목록");

            // ── 시트 3: 위험 발주 ──
            Sheet warnSheet = workbook.createSheet("위험 발주");
            writeOrderSheet(warnSheet, report.getWarningOrders(), headerStyle, warningStyle, "🟠 위험 발주 목록");

            // ── 시트 4: 전체 목록 ──
            Sheet allSheet = workbook.createSheet("전체 발주 목록");
            writeOrderSheet(allSheet, orderService.findAll(), headerStyle, normalStyle, "📋 전체 발주 목록");

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[리포트] 엑셀 생성 실패", e);
            throw new CustomException(ErrorCode.REPORT_GENERATE_FAILED);
        }
    }

    // ───────────────────────────────────────
    // 시트 작성 헬퍼
    // ───────────────────────────────────────
    private void writeSummarySheet(Sheet sheet, ReportResponse report,
                                   CellStyle titleStyle, CellStyle headerStyle) {
        int row = 0;

        // 제목
        Row titleRow = sheet.createRow(row++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("납기 모니터링 리포트 — " + report.getReportDate());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        row++; // 빈 행

        // KPI 헤더
        Row kpiHeader = sheet.createRow(row++);
        String[] kpiCols = {"항목", "건수", "비율"};
        for (int i = 0; i < kpiCols.length; i++) {
            Cell c = kpiHeader.createCell(i);
            c.setCellValue(kpiCols[i]);
            c.setCellStyle(headerStyle);
        }

        // KPI 데이터
        int total = report.getTotalCount();
        Object[][] kpiData = {
                {"전체 발주", report.getTotalCount(), "-"},
                {"입고 완료", report.getReceivedCount(), pct(report.getReceivedCount(), total)},
                {"지연", report.getDelayedCount(), pct(report.getDelayedCount(), total)},
                {"위험", report.getWarningCount(), pct(report.getWarningCount(), total)},
                {"정상", report.getNormalCount(), pct(report.getNormalCount(), total)},
                {"지연율", "-", report.getDelayRate() + "%"},
        };

        for (Object[] kpi : kpiData) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(kpi[0].toString());
            r.createCell(1).setCellValue(kpi[1].toString());
            r.createCell(2).setCellValue(kpi[2].toString());
        }

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 3000);
    }

    private void writeOrderSheet(Sheet sheet, List<OrderResponse> orders,
                                 CellStyle headerStyle, CellStyle dataStyle, String title) {
        int row = 0;

        // 제목 행
        Row titleRow = sheet.createRow(row++);
        titleRow.createCell(0).setCellValue(title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
        row++;

        // 헤더
        Row header = sheet.createRow(row++);
        String[] cols = {"PO번호", "품목", "협력사", "발주일", "납기일", "진행상태", "위험상태", "입고여부"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(headerStyle);
        }

        // 데이터
        for (OrderResponse o : orders) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(o.getPoNumber());
            r.createCell(1).setCellValue(o.getItem());
            r.createCell(2).setCellValue(o.getVendor());
            r.createCell(3).setCellValue(o.getOrderDate() != null ? o.getOrderDate().toString() : "-");
            r.createCell(4).setCellValue(o.getDueDate() != null ? o.getDueDate().toString() : "-");
            r.createCell(5).setCellValue(o.getProgressStatusDescription());
            r.createCell(6).setCellValue(o.getRiskStatusDescription());
            r.createCell(7).setCellValue(o.isReceived() ? "Y" : "N");
            for (int i = 0; i <= 7; i++) r.getCell(i).setCellStyle(dataStyle);
        }

        int[] widths = {4000, 5000, 4500, 3500, 3500, 3500, 3500, 3000};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);
    }

    // ───────────────────────────────────────
    // 스타일 팩토리
    // ───────────────────────────────────────
    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        return s;
    }

    private CellStyle createColorStyle(Workbook wb, IndexedColors color) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(color.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private String pct(int count, int total) {
        if (total == 0) return "0%";
        return Math.round(count * 1000.0 / total) / 10.0 + "%";
    }
}
