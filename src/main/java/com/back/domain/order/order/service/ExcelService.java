package com.back.domain.order.order.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final OrderRepository orderRepository;

    public void upload(MultipartFile file) {

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 1행 = 헤더
                Row row = sheet.getRow(i);
                if (row == null) continue;

                LocalDate orderDate = getLocalDate(row.getCell(3));
                LocalDate dueDate = getLocalDate(row.getCell(4));

                Order order = Order.builder()
                        .poNumber(getString(row.getCell(0)))
                        .vendor(getString(row.getCell(1)))
                        .item(getString(row.getCell(2)))
                        .orderDate(orderDate)
                        .dueDate(dueDate)
                        .received("Y".equalsIgnoreCase(getString(row.getCell(5))))
                        .progressStatus(OrderStatus.NOT_STARTED)
                        .riskStatus(RiskStatus.NORMAL)
                        .build();

                orderRepository.save(order);
            }

        } catch (Exception e) {
            throw new RuntimeException("엑셀 업로드 실패", e);
        }
    }

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