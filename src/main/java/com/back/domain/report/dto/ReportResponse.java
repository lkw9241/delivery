package com.back.domain.report.dto;

import com.back.domain.order.order.dto.response.OrderResponse;
import com.back.domain.order.order.enums.RiskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ReportResponse {

    // 기준일
    private LocalDate reportDate;

    // KPI
    private int totalCount;
    private int receivedCount;
    private int delayedCount;
    private int warningCount;
    private int normalCount;

    // 지연율 (소수점 1자리)
    private double delayRate;

    // 상세 목록
    private List<OrderResponse> delayedOrders;
    private List<OrderResponse> warningOrders;
    private List<OrderResponse> normalOrders;

    public static ReportResponse of(List<OrderResponse> allOrders) {
        LocalDate today = LocalDate.now();

        List<OrderResponse> delayed = allOrders.stream()
                .filter(o -> o.getRiskStatus() == RiskStatus.DELAYED).toList();
        List<OrderResponse> warning = allOrders.stream()
                .filter(o -> o.getRiskStatus() == RiskStatus.WARNING).toList();
        List<OrderResponse> normal = allOrders.stream()
                .filter(o -> o.getRiskStatus() == RiskStatus.NORMAL && !o.isReceived()).toList();
        long received = allOrders.stream().filter(OrderResponse::isReceived).count();

        int total = allOrders.size();
        double delayRate = total == 0 ? 0.0
                : Math.round((delayed.size() * 1000.0 / total)) / 10.0;

        return ReportResponse.builder()
                .reportDate(today)
                .totalCount(total)
                .receivedCount((int) received)
                .delayedCount(delayed.size())
                .warningCount(warning.size())
                .normalCount(normal.size())
                .delayRate(delayRate)
                .delayedOrders(delayed)
                .warningOrders(warning)
                .normalOrders(normal)
                .build();
    }
}
