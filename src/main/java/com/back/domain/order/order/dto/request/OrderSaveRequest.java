package com.back.domain.order.order.dto.request;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class OrderSaveRequest {

    private String poNumber;
    private String vendor;
    private String item;
    private LocalDate orderDate;
    private LocalDate dueDate;
    private boolean received;

    public Order toEntity() {
        return Order.builder()
                .poNumber(this.poNumber)
                .vendor(this.vendor)
                .item(this.item)
                .orderDate(this.orderDate)
                .dueDate(this.dueDate)
                .received(this.received)
                .progressStatus(OrderStatus.NOT_STARTED)
                .riskStatus(RiskStatus.NORMAL)
                .build();
    }
}
