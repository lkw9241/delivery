package com.back.domain.notification.controller;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    // ───────────────────────────────────────
    // 테스트 픽스처
    // ───────────────────────────────────────

    private NotificationResponse buildNotificationResponse(Long id, boolean read,
                                                            NotificationType type) {
        return NotificationResponse.builder()
                .id(id)
                .orderId(10L)
                .poNumber("PO-001")
                .vendor("테스트업체")
                .item("테스트품목")
                .dueDate(LocalDate.of(2026, 3, 20))
                .notificationType(type)
                .notificationTypeDescription(type.getDescription())
                .message("[" + type.getDescription() + "] PO-001 - 테스트업체 (납기일: 2026-03-20)")
                .notifiedAt(LocalDateTime.of(2026, 3, 23, 9, 0))
                .read(read)
                .build();
    }

    // ───────────────────────────────────────
    // GET /notifications
    // ───────────────────────────────────────

    @Nested
    @DisplayName("GET /notifications — 알림 목록 조회")
    class ListNotifications {

        @Test
        @DisplayName("알림이 있을 때 목록과 미읽음 수를 모델에 담아 반환한다")
        void list_withNotifications_returnsModelWithData() throws Exception {
            // given
            List<NotificationResponse> notifications = List.of(
                    buildNotificationResponse(1L, false, NotificationType.DELAYED),
                    buildNotificationResponse(2L, false, NotificationType.WARNING),
                    buildNotificationResponse(3L, true,  NotificationType.DELAYED)
            );

            when(notificationService.findAll()).thenReturn(notifications);
            when(notificationService.countUnread()).thenReturn(2L);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("notifications"))
                    .andExpect(model().attribute("notifications", hasSize(3)))
                    .andExpect(model().attribute("unreadCount", 2L));

            verify(notificationService, times(1)).findAll();
            verify(notificationService, times(1)).countUnread();
        }

        @Test
        @DisplayName("알림이 없을 때 빈 목록과 0을 모델에 담아 반환한다")
        void list_withNoNotifications_returnsEmptyModel() throws Exception {
            // given
            when(notificationService.findAll()).thenReturn(List.of());
            when(notificationService.countUnread()).thenReturn(0L);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("notifications"))
                    .andExpect(model().attribute("notifications", hasSize(0)))
                    .andExpect(model().attribute("unreadCount", 0L));
        }

        @Test
        @DisplayName("미읽음 알림만 있을 때 unreadCount가 정확히 표시된다")
        void list_allUnread_unreadCountMatchesTotal() throws Exception {
            // given
            List<NotificationResponse> notifications = List.of(
                    buildNotificationResponse(1L, false, NotificationType.DELAYED),
                    buildNotificationResponse(2L, false, NotificationType.DELAYED)
            );

            when(notificationService.findAll()).thenReturn(notifications);
            when(notificationService.countUnread()).thenReturn(2L);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("unreadCount", 2L));
        }

        @Test
        @DisplayName("모두 읽음 처리된 상태면 unreadCount가 0이다")
        void list_allRead_unreadCountIsZero() throws Exception {
            // given
            List<NotificationResponse> notifications = List.of(
                    buildNotificationResponse(1L, true, NotificationType.DELAYED),
                    buildNotificationResponse(2L, true, NotificationType.WARNING)
            );

            when(notificationService.findAll()).thenReturn(notifications);
            when(notificationService.countUnread()).thenReturn(0L);

            // when & then
            mockMvc.perform(get("/notifications"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("notifications", hasSize(2)))
                    .andExpect(model().attribute("unreadCount", 0L));
        }
    }

    // ───────────────────────────────────────
    // POST /notifications/{id}/read
    // ───────────────────────────────────────

    @Nested
    @DisplayName("POST /notifications/{id}/read — 단건 읽음 처리")
    class MarkRead {

        @Test
        @DisplayName("존재하는 알림 ID로 읽음 처리하면 /notifications 로 리다이렉트된다")
        void markRead_validId_redirectsToList() throws Exception {
            // given
            Long notificationId = 1L;
            doNothing().when(notificationService).markRead(notificationId);

            // when & then
            mockMvc.perform(post("/notifications/{id}/read", notificationId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/notifications"));

            verify(notificationService, times(1)).markRead(notificationId);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 읽음 처리하면 CustomException(N001)이 발생한다")
        void markRead_invalidId_throwsCustomException() throws Exception {
            // given
            Long invalidId = 999L;
            doThrow(new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND,
                    "알림 ID [" + invalidId + "]를 찾을 수 없습니다."))
                    .when(notificationService).markRead(invalidId);

            // when & then
            mockMvc.perform(post("/notifications/{id}/read", invalidId))
                    .andExpect(status().isNotFound());

            verify(notificationService, times(1)).markRead(invalidId);
        }

        @Test
        @DisplayName("이미 읽음 처리된 알림도 markRead 호출 후 리다이렉트된다")
        void markRead_alreadyRead_stillRedirects() throws Exception {
            // given — 서비스가 예외 없이 처리 (멱등 처리)
            Long alreadyReadId = 2L;
            doNothing().when(notificationService).markRead(alreadyReadId);

            // when & then
            mockMvc.perform(post("/notifications/{id}/read", alreadyReadId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/notifications"));
        }
    }

    // ───────────────────────────────────────
    // POST /notifications/read-all
    // ───────────────────────────────────────

    @Nested
    @DisplayName("POST /notifications/read-all — 전체 읽음 처리")
    class MarkAllRead {

        @Test
        @DisplayName("전체 읽음 처리 후 /notifications 로 리다이렉트된다")
        void markAllRead_success_redirectsToList() throws Exception {
            // given
            doNothing().when(notificationService).markAllRead();

            // when & then
            mockMvc.perform(post("/notifications/read-all"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/notifications"));

            verify(notificationService, times(1)).markAllRead();
        }

        @Test
        @DisplayName("알림이 없을 때 전체 읽음 처리해도 정상 리다이렉트된다")
        void markAllRead_noNotifications_stillRedirects() throws Exception {
            // given — 알림이 없어도 서비스는 정상 동작
            doNothing().when(notificationService).markAllRead();

            // when & then
            mockMvc.perform(post("/notifications/read-all"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/notifications"));
        }

        @Test
        @DisplayName("전체 읽음 처리 시 서비스가 정확히 1번 호출된다")
        void markAllRead_callsServiceExactlyOnce() throws Exception {
            // given
            doNothing().when(notificationService).markAllRead();

            // when
            mockMvc.perform(post("/notifications/read-all"));

            // then
            verify(notificationService, times(1)).markAllRead();
            verifyNoMoreInteractions(notificationService);
        }
    }
}
