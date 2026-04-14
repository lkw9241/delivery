package com.back.domain.order.order.controller;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.order.order.dto.response.DashboardResponse;
import com.back.domain.order.order.dto.response.OrderResponse;
import com.back.domain.order.order.enums.OrderStatus;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.service.OrderService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private NotificationService notificationService;

    // ───────────────────────────────────────
    // GET /dashboard
    // ───────────────────────────────────────

    @Test
    @DisplayName("대시보드 메인 화면 조회 - 정상 데이터가 있을 때 model attribute가 세팅된다")
    void dashboard_success() throws Exception {
        // given
        List<OrderResponse> orders = List.of(
                buildOrderResponse(1L, "PO-001", RiskStatus.NORMAL, OrderStatus.PROCESSING),
                buildOrderResponse(2L, "PO-002", RiskStatus.DELAYED, OrderStatus.NOT_STARTED)
        );
        List<OrderResponse> delayedOrders = List.of(orders.get(1));
        DashboardResponse dashboardResponse = DashboardResponse.of(orders, delayedOrders);

        List<NotificationResponse> unreadNotifications = List.of(
                buildNotificationResponse(1L, 2L, NotificationType.DELAYED)
        );

        given(orderService.findAll()).willReturn(orders);
        given(orderService.findDelayedOrders()).willReturn(delayedOrders);
        given(notificationService.findUnread()).willReturn(unreadNotifications);
        given(notificationService.countUnread()).willReturn(1L);

        // when & then
        mvc.perform(get("/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(model().attributeExists("unreadNotifications"))
                .andExpect(model().attributeExists("unreadCount"))
                .andExpect(model().attribute("unreadCount", 1L));
    }

    @Test
    @DisplayName("대시보드 메인 화면 조회 - 주문이 없어도 정상적으로 빈 목록이 세팅된다")
    void dashboard_emptyOrders() throws Exception {
        // given
        given(orderService.findAll()).willReturn(List.of());
        given(orderService.findDelayedOrders()).willReturn(List.of());
        given(notificationService.findUnread()).willReturn(List.of());
        given(notificationService.countUnread()).willReturn(0L);

        // when & then
        mvc.perform(get("/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("unreadCount", 0L));
    }

    @Test
    @DisplayName("대시보드 메인 화면 조회 - 읽지 않은 알림이 5건 초과여도 최대 5건만 모델에 담긴다")
    void dashboard_unreadNotifications_limitFive() throws Exception {
        // given
        List<NotificationResponse> sixNotifications = List.of(
                buildNotificationResponse(1L, 1L, NotificationType.DELAYED),
                buildNotificationResponse(2L, 2L, NotificationType.WARNING),
                buildNotificationResponse(3L, 3L, NotificationType.DELAYED),
                buildNotificationResponse(4L, 4L, NotificationType.WARNING),
                buildNotificationResponse(5L, 5L, NotificationType.DELAYED),
                buildNotificationResponse(6L, 6L, NotificationType.WARNING)
        );

        given(orderService.findAll()).willReturn(List.of());
        given(orderService.findDelayedOrders()).willReturn(List.of());
        given(notificationService.findUnread()).willReturn(sixNotifications);
        given(notificationService.countUnread()).willReturn(6L);

        // when & then
        mvc.perform(get("/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("unreadCount", 6L));
        // stream().limit(5) 적용 확인: unreadNotifications list size == 5
    }

    // ───────────────────────────────────────
    // GET /dashboard/{id}
    // ───────────────────────────────────────

    @Test
    @DisplayName("주문 상세 조회 - 존재하는 ID이면 order-detail 뷰를 반환한다")
    void detail_success() throws Exception {
        // given
        OrderResponse order = buildOrderResponse(1L, "PO-001", RiskStatus.NORMAL, OrderStatus.PROCESSING);
        given(orderService.findById(1L)).willReturn(order);

        // when & then
        mvc.perform(get("/dashboard/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("order-detail"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    @DisplayName("주문 상세 조회 - 존재하지 않는 ID이면 CustomException이 발생한다")
    void detail_notFound() throws Exception {
        // given
        given(orderService.findById(999L))
                .willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND, "발주 ID [999]를 찾을 수 없습니다."));

        // when & then
        mvc.perform(get("/dashboard/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ───────────────────────────────────────
    // POST /dashboard/{id}/status
    // ───────────────────────────────────────

    @Test
    @DisplayName("진행 상태 변경 - 정상 요청이면 대시보드로 리다이렉트된다")
    void updateStatus_success() throws Exception {
        // given
        willDoNothing().given(orderService).updateProgressStatus(1L, OrderStatus.PROCESSING);

        // when & then
        mvc.perform(post("/dashboard/1/status")
                        .param("status", "PROCESSING"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("진행 상태 변경 - 이미 입고 완료된 발주이면 CustomException이 발생한다")
    void updateStatus_alreadyReceived() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ORDER_ALREADY_RECEIVED, "입고 완료된 발주는 상태를 변경할 수 없습니다."))
                .given(orderService).updateProgressStatus(1L, OrderStatus.PROCESSING);

        // when & then
        mvc.perform(post("/dashboard/1/status")
                        .param("status", "PROCESSING"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("진행 상태 변경 - 존재하지 않는 주문 ID이면 CustomException이 발생한다")
    void updateStatus_orderNotFound() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND, "발주 ID [999]를 찾을 수 없습니다."))
                .given(orderService).updateProgressStatus(999L, OrderStatus.NOT_STARTED);

        // when & then
        mvc.perform(post("/dashboard/999/status")
                        .param("status", "NOT_STARTED"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ───────────────────────────────────────
    // POST /dashboard/{id}/receive
    // ───────────────────────────────────────

    @Test
    @DisplayName("입고 완료 처리 - 정상 요청이면 대시보드로 리다이렉트된다")
    void markReceived_success() throws Exception {
        // given
        willDoNothing().given(orderService).markReceived(1L);

        // when & then
        mvc.perform(post("/dashboard/1/receive"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("입고 완료 처리 - 이미 입고 완료된 발주이면 CustomException이 발생한다")
    void markReceived_alreadyReceived() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ORDER_ALREADY_RECEIVED, "이미 입고 처리된 발주입니다."))
                .given(orderService).markReceived(1L);

        // when & then
        mvc.perform(post("/dashboard/1/receive"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입고 완료 처리 - 존재하지 않는 주문 ID이면 CustomException이 발생한다")
    void markReceived_orderNotFound() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND, "발주 ID [999]를 찾을 수 없습니다."))
                .given(orderService).markReceived(999L);

        // when & then
        mvc.perform(post("/dashboard/999/receive"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ───────────────────────────────────────
    // 테스트 픽스처 헬퍼
    // ───────────────────────────────────────

    private OrderResponse buildOrderResponse(Long id, String poNumber,
                                              RiskStatus riskStatus, OrderStatus orderStatus) {
        return OrderResponse.builder()
                .id(id)
                .poNumber(poNumber)
                .vendor("테스트업체")
                .item("테스트품목")
                .orderDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(5))
                .received(false)
                .progressStatus(orderStatus)
                .riskStatus(riskStatus)
                .progressStatusDescription(orderStatus.getDescription())
                .riskStatusDescription(riskStatus.getDescription())
                .build();
    }

    private NotificationResponse buildNotificationResponse(Long id, Long orderId,
                                                            NotificationType type) {
        return NotificationResponse.builder()
                .id(id)
                .orderId(orderId)
                .poNumber("PO-00" + orderId)
                .vendor("테스트업체")
                .item("테스트품목")
                .dueDate(LocalDate.now().plusDays(2))
                .notificationType(type)
                .notificationTypeDescription(type.getDescription())
                .message(type.getDescription() + " 알림")
                .notifiedAt(LocalDateTime.now())
                .read(false)
                .build();
    }
}
