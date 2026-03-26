package com.back.domain.order.order.service;

import com.back.domain.order.order.dto.request.OrderSaveRequest;
import com.back.domain.order.order.dto.response.OrderResponse;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.util.RiskEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final RiskEvaluator riskEvaluator;

    // 전체조회
    public List<OrderResponse> findAll() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(riskEvaluator::evaluate);
        return orders.stream().map(OrderResponse::from).toList();
    }

    // 단건조회
    public OrderResponse findById(Long id) {
        Order order = findOrderById(id);
        riskEvaluator.evaluate(order);
        return OrderResponse.from(order);
    }

    // 신규 저장
    @Transactional
    public OrderResponse save(OrderSaveRequest request) {
        Order order = request.toEntity();
        riskEvaluator.evaluate(order);
        return OrderResponse.from(orderRepository.save(order));
    }

    // 진행 상태 변경
    @Transactional
    public void updateProgressStatus(Long id, OrderStatus status) {
        Order order = findOrderById(id);

        if (order.isReceived()) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_RECEIVED,
                    "입고 완료된 발주는 상태를 변경할 수 없습니다. (ID: " + id + ")");
        }
        if (status == null) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS_TRANSITION,
                    "변경할 상태값이 유효하지 않습니다.");
        }

        order.updateProgressStatus(status);
        riskEvaluator.evaluate(order);
    }

    // 입고 처리
    @Transactional
    public void markReceived(Long id) {
        Order order = findOrderById(id);

        if (order.isReceived()) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_RECEIVED,
                    "이미 입고 처리된 발주입니다. (ID: " + id + ")");
        }

        order.markReceived();
        order.updateRiskStatus(RiskStatus.NORMAL);
    }

    // 지연 주문 조회
    public List<OrderResponse> findDelayedOrders() {
        List<Order> orders = orderRepository.findAll();
        orders.forEach(riskEvaluator::evaluate);
        return orders.stream()
                .filter(o -> o.getRiskStatus() == RiskStatus.DELAYED)
                .map(OrderResponse::from)
                .toList();
    }

    // 내부 전용 — Entity 조회
    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND,
                        "발주 ID [" + id + "]를 찾을 수 없습니다."));
    }
}
