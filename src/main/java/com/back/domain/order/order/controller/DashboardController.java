package com.back.domain.order.order.controller;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.order.order.dto.response.DashboardResponse;
import com.back.domain.order.order.dto.response.OrderResponse;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
    public class DashboardController {
    private final OrderService orderService;
    private final NotificationService notificationService;

    // ───────────────────────────────────────
    // 대시보드 메인 화면
    // ───────────────────────────────────────
    @GetMapping
    public String dashboard(Model model) {
        // 발주 데이터
        List<OrderResponse> orders = orderService.findAll();
        List<OrderResponse> delayedOrders = orderService.findDelayedOrders();
        DashboardResponse dashboard = DashboardResponse.of(orders, delayedOrders);

        // 알림 데이터 (읽지 않은 알림 최대 5건만 패널에 표시)
        List<NotificationResponse> unreadNotifications = notificationService.findUnread()
                .stream()
                .limit(5)
                .toList();
        long unreadCount = notificationService.countUnread();

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("unreadCount", unreadCount);

        return "dashboard";
    }

    // ───────────────────────────────────────
    // 주문 상세 조회
    // ───────────────────────────────────────
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderResponse order = orderService.findById(id);
        model.addAttribute("order", order);
        return "order-detail";
    }

    // ───────────────────────────────────────
    // 진행 상태 변경
    // ───────────────────────────────────────
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        orderService.updateProgressStatus(id, status);
        return "redirect:/dashboard";
    }

    // ───────────────────────────────────────
    // 입고 완료 처리
    // ───────────────────────────────────────
    @PostMapping("/{id}/receive")
    public String markReceived(@PathVariable Long id) {
        orderService.markReceived(id);
        return "redirect:/dashboard";
    }
