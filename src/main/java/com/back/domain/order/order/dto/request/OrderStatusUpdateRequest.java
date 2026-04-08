package com.back.domain.order.order.dto.request;

import com.back.domain.order.order.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequest {

    private OrderStatus status;
}
