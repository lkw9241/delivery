package com.back.domain.notification.entity;

import com.back.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        // 동일 발주 + 동일 유형 + 동일 날짜 중복 알림 방지
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"order_id", "notification_type", "notified_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관 발주 ID (FK 없이 ID만 보관 — 도메인 간 느슨한 결합)
    @Column(nullable = false)
    private Long orderId;

    // 스냅샷: 알림 시점의 발주 정보
    @Column(nullable = false)
    private String poNumber;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private String item;

    private LocalDate dueDate;

    // 알림 유형 (WARNING / DELAYED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    // 알림 메시지
    @Column(nullable = false)
    private String message;

    // 알림 발생 일시
    @Column(nullable = false)
    private LocalDateTime notifiedAt;

    // 알림 발생 날짜 (중복 방지 unique key 용)
    @Column(nullable = false)
    private LocalDate notifiedDate;

    // 읽음 여부
    private boolean read;

    // ───────────────────────────────────────
    // 팩토리 메서드
    // ───────────────────────────────────────
    public static Notification create(
            Long orderId,
            String poNumber,
            String vendor,
            String item,
            LocalDate dueDate,
            NotificationType type
    ) {
        LocalDateTime now = LocalDateTime.now();
        return Notification.builder()
                .orderId(orderId)
                .poNumber(poNumber)
                .vendor(vendor)
                .item(item)
                .dueDate(dueDate)
                .notificationType(type)
                .message(buildMessage(poNumber, vendor, dueDate, type))
                .notifiedAt(now)
                .notifiedDate(now.toLocalDate())
                .read(false)
                .build();
    }

    // 읽음 처리
    public void markRead() {
        this.read = true;
    }

    // ───────────────────────────────────────
    // 메시지 생성
    // ───────────────────────────────────────
    private static String buildMessage(
            String poNumber,
            String vendor,
            LocalDate dueDate,
            NotificationType type
    ) {
        return String.format("[%s] %s - %s (납기일: %s)",
                type.getDescription(), poNumber, vendor,
                dueDate != null ? dueDate.toString() : "미정");
    }
}
