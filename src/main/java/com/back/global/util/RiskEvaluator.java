package com.back.global.util;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.RiskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 납기 위험도 판단 로직 공통 컴포넌트
 * OrderService / NotificationService 양쪽에서 동일하게 사용
 * <p>
 * 판단 기준:
 * - 입고 완료          → NORMAL
 * - 납기일 초과         → DELAYED
 * - 납기일 3일 이내     → WARNING
 * - 그 외              → NORMAL
 */
@Component
public class RiskEvaluator {

    /**
     * Order 엔티티의 riskStatus 필드를 직접 갱신한다.
     */
    public void evaluate(Order order) {
        order.updateRiskStatus(calculateRisk(order, LocalDate.now()));
    }

    /**
     * 날짜를 주입받아 RiskStatus를 계산해 반환한다. (테스트 용이성)
     */
    public RiskStatus calculateRisk(Order order, LocalDate today) {
        if (order.isReceived()) {
            return RiskStatus.NORMAL;
        }

        LocalDate dueDate = order.getDueDate();
        if (dueDate == null) {
            return RiskStatus.NORMAL;
        }
        if (today.isAfter(dueDate)) {
            return RiskStatus.DELAYED;
        } else if (today.plusDays(3).isAfter(dueDate)) {
            return RiskStatus.WARNING;
        } else {
            return RiskStatus.NORMAL;
        }
    }
}
