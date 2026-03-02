package com.back.domain.order.order.service;

import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

   //전체 조회
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * 단건 조회
     */
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 발주가 존재하지 않습니다."));
    }

    /**
     * 신규 저장
     */
    @Transactional
    public Order save(Order order) {
        evaluateRisk(order);  // 저장 전에 위험도 계산
        return orderRepository.save(order);
    }

    /**
     * 협력업체에서 가공 진행 상태 변경
     */
    @Transactional
    public void updateProgressStatus(Long id, OrderStatus status) {
        Order order = findById(id);
        order.updateProgressStatus(status);
        evaluateRisk(order);
    }

    /**
     * 입고 처리
     */
    @Transactional
    public void markReceived(Long id) {
        Order order = findById(id);
        order.markReceived();
        order.updateRiskStatus(RiskStatus.NORMAL);
    }

    /**
     * 지연 주문 조회
     */
    public List<Order> findDelayedOrders() {
        return orderRepository.findByRiskStatus(RiskStatus.DELAYED);
    }


    //위험도 판단 로직 (핵심 비즈니스)

    private void evaluateRisk(Order order) {

        if (order.isReceived()) {
            order.updateRiskStatus(RiskStatus.NORMAL);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = order.getDueDate();

        if (dueDate == null) {
            order.updateRiskStatus(RiskStatus.NORMAL);
            return;
        }

        if (today.isAfter(dueDate)) {
            order.updateRiskStatus(RiskStatus.DELAYED);
        } else if (today.plusDays(3).isAfter(dueDate)) {
            order.updateRiskStatus(RiskStatus.WARNING);
        } else {
            order.updateRiskStatus(RiskStatus.NORMAL);
        }
    }
}