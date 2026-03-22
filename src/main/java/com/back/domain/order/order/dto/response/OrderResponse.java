package com.back.domain.order.order.dto.response;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private String poNumber;
    private String vendor;
    private String item;
    private LocalDate orderDate;
    private LocalDate dueDate;
    private boolean received;

    // Enum 값 (코드)
    private OrderStatus progressStatus;
    private RiskStatus riskStatus;

    // Enum 한글 설명
    private String progressStatusDescription;
    private String riskStatusDescription;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .poNumber(order.getPoNumber())
                .vendor(order.getVendor())
                .item(order.getItem())
                .orderDate(order.getOrderDate())
                .dueDate(order.getDueDate())
                .received(order.isReceived())
                .progressStatus(order.getProgressStatus())
                .riskStatus(order.getRiskStatus())
                .progressStatusDescription(
                        order.getProgressStatus() != null
                                ? order.getProgressStatus().getDescription()
                                : null)
                .riskStatusDescription(
                        order.getRiskStatus() != null
                                ? order.getRiskStatus().getDescription()
                                : null)
                .build();
    }
}
