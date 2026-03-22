package com.back.domain.order.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    // KPI
    private int totalCount;
    private int delayCount;
    private int normalCount;
    private int warningCount;

    // 전체 주문 목록
    private List<OrderResponse> orders;

    // 지연 주문 목록
    private List<OrderResponse> delayedOrders;

    public static DashboardResponse of(List<OrderResponse> orders, List<OrderResponse> delayedOrders) {

        long warningCount = orders.stream()
                .filter(o -> o.getRiskStatus() != null)
                .filter(o -> "WARNING".equals(o.getRiskStatus().name()))
                .count();

        return DashboardResponse.builder()
                .totalCount(orders.size())
                .delayCount(delayedOrders.size())
                .normalCount(orders.size() - delayedOrders.size())
                .warningCount((int) warningCount)
                .orders(orders)
                .delayedOrders(delayedOrders)
                .build();
    }
}
