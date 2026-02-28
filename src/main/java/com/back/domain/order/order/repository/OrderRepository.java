package com.back.domain.order.order.repository;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

    public interface OrderRepository extends JpaRepository<Order, Long> {

        // 특정 협력사 조회
        List<Order> findByVendor(String vendor);

        // 납기일 기준 조회
        List<Order> findByDueDateBefore(LocalDate date);

        // 위험 상태별 조회
        List<Order> findByRiskStatus(RiskStatus riskStatus);

        // 진행 상태별 조회
        List<Order> findByProgressStatus(OrderStatus status);
    }
