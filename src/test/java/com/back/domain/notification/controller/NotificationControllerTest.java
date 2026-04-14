package com.back.domain.notification.controller;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
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
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NotificationService notificationService;

    // ───────────────────────────────────────
    // GET /notifications
    // ───────────────────────────────────────

    @Test
    @DisplayName("알림 목록 조회 - 알림이 있을 때 model attribute가 정상적으로 세팅된다")
    void list_success() throws Exception {
        // given
        List<NotificationResponse> notifications = List.of(
                buildNotificationResponse(1L, 1L, NotificationType.DELAYED, false),
                buildNotificationResponse(2L, 2L, NotificationType.WARNING, false),
                buildNotificationResponse(3L, 3L, NotificationType.WARNING, true)
        );

        given(notificationService.findAll()).willReturn(notifications);
        given(notificationService.countUnread()).willReturn(2L);

        // when & then
        mvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("unreadCount"))
                .andExpect(model().attribute("unreadCount", 2L));
    }

    @Test
    @DisplayName("알림 목록 조회 - 알림이 없어도 빈 목록과 unreadCount 0이 정상적으로 세팅된다")
    void list_empty() throws Exception {
        // given
        given(notificationService.findAll()).willReturn(List.of());
        given(notificationService.countUnread()).willReturn(0L);

        // when & then
        mvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attribute("unreadCount", 0L));
    }

    @Test
    @DisplayName("알림 목록 조회 - 모든 알림이 읽음 처리된 경우 unreadCount가 0이다")
    void list_allRead() throws Exception {
        // given
        List<NotificationResponse> allReadNotifications = List.of(
                buildNotificationResponse(1L, 1L, NotificationType.DELAYED, true),
                buildNotificationResponse(2L, 2L, NotificationType.WARNING, true)
        );

        given(notificationService.findAll()).willReturn(allReadNotifications);
        given(notificationService.countUnread()).willReturn(0L);

        // when & then
        mvc.perform(get("/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attribute("unreadCount", 0L));
    }

    // ───────────────────────────────────────
    // POST /notifications/{id}/read
    // ───────────────────────────────────────

    @Test
    @DisplayName("단건 읽음 처리 - 정상 요청이면 알림 목록으로 리다이렉트된다")
    void markRead_success() throws Exception {
        // given
        willDoNothing().given(notificationService).markRead(1L);

        // when & then
        mvc.perform(post("/notifications/1/read"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    @DisplayName("단건 읽음 처리 - 존재하지 않는 알림 ID이면 NOTIFICATION_NOT_FOUND 예외가 발생한다")
    void markRead_notFound() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND,
                "알림 ID [999]를 찾을 수 없습니다."))
                .given(notificationService).markRead(999L);

        // when & then
        mvc.perform(post("/notifications/999/read"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ───────────────────────────────────────
    // POST /notifications/read-all
    // ───────────────────────────────────────

    @Test
    @DisplayName("전체 읽음 처리 - 정상 요청이면 알림 목록으로 리다이렉트된다")
    void markAllRead_success() throws Exception {
        // given
        willDoNothing().given(notificationService).markAllRead();

        // when & then
        mvc.perform(post("/notifications/read-all"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    @DisplayName("전체 읽음 처리 - 읽지 않은 알림이 없어도 정상적으로 리다이렉트된다")
    void markAllRead_noUnread() throws Exception {
        // given
        willDoNothing().given(notificationService).markAllRead();

        // when & then
        mvc.perform(post("/notifications/read-all"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    // ───────────────────────────────────────
    // 테스트 픽스처 헬퍼
    // ───────────────────────────────────────

    private NotificationResponse buildNotificationResponse(Long id, Long orderId,
                                                            NotificationType type, boolean read) {
        return NotificationResponse.builder()
                .id(id)
                .orderId(orderId)
                .poNumber("PO-00" + orderId)
                .vendor("테스트업체")
                .item("테스트품목")
                .dueDate(LocalDate.now().plusDays(2))
                .notificationType(type)
                .notificationTypeDescription(type.getDescription())
                .message(type.getDefaultMessage())
                .notifiedAt(LocalDateTime.now())
                .read(read)
                .build();
    }
}
