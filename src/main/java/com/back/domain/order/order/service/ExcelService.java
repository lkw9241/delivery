package com.back.domain.order.order.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private static final String[] ALLOWED_EXTENSIONS = {".xls", ".xlsx"};

    private final OrderRepository orderRepository;

    /**
     * 엑셀 업로드 — 전체 행을 하나의 트랜잭션으로 처리
     * 중간 행에서 실패하면 전체 롤백됨 (partial 저장 방지)
     */
    @Transactional
    public void upload(MultipartFile file) {

        validateFile(file);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 파싱을 먼저 전부 완료한 뒤 일괄 저장
            List<Order> orders = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String poNumber = getString(row.getCell(0));
                String vendor = getString(row.getCell(1));
                String item = getString(row.getCell(2));

                // 필수값 검증 — 실패 시 트랜잭션 전체 롤백
                if (poNumber.isBlank() || vendor.isBlank() || item.isBlank()) {
                    throw new CustomException(ErrorCode.EXCEL_INVALID_ROW,
                            (i + 1) + "번째 행에 필수값(PO번호, 협력사, 품목)이 누락되었습니다.");
                }

                LocalDate orderDate = getLocalDate(row.getCell(3));
                LocalDate dueDate = getLocalDate(row.getCell(4));

                orders.add(Order.builder()
                        .poNumber(poNumber)
                        .vendor(vendor)
                        .item(item)
                        .orderDate(orderDate)
                        .dueDate(dueDate)
                        .received("Y".equalsIgnoreCase(getString(row.getCell(5))))
                        .progressStatus(OrderStatus.NOT_STARTED)
                        .riskStatus(RiskStatus.NORMAL)
                        .build());
            }

            // 파싱 완료 후 일괄 저장
            orderRepository.saveAll(orders);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXCEL_PARSE_FAILED,
                    "엑셀 파일 파싱 중 오류가 발생했습니다. 파일 형식을 확인해주세요.");
        }
    }

    // ───────────────────────────────────────
    // 파일 유효성 검증
    // ───────────────────────────────────────
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.EXCEL_EMPTY_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException(ErrorCode.EXCEL_INVALID_FORMAT);
        }

        String lowerName = originalFilename.toLowerCase();
        boolean validExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }

        if (!validExtension) {
            throw new CustomException(ErrorCode.EXCEL_INVALID_FORMAT,
                    "허용되지 않는 파일 형식입니다. (.xls, .xlsx 만 업로드 가능)");
        }
    }

    // ───────────────────────────────────────
    // 셀 파싱 헬퍼
    // ───────────────────────────────────────
    private String getString(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }

    private LocalDate getLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        return null;
    }
}
