package com.back.domain.notification.dto.response;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private Long orderId;
    private String poNumber;
    private String vendor;
    private String item;
    private LocalDate dueDate;
    private NotificationType notificationType;
    private String notificationTypeDescription;
    private String message;
    private LocalDateTime notifiedAt;
    private boolean read;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .orderId(notification.getOrderId())
                .poNumber(notification.getPoNumber())
                .vendor(notification.getVendor())
                .item(notification.getItem())
                .dueDate(notification.getDueDate())
                .notificationType(notification.getNotificationType())
                .notificationTypeDescription(notification.getNotificationType().getDescription())
                .message(notification.getMessage())
                .notifiedAt(notification.getNotifiedAt())
                .read(notification.isRead())
                .build();
    }
}
