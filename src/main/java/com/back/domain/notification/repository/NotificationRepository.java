package com.back.domain.notification.repository;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 오늘 특정 발주 + 유형의 알림이 이미 존재하는지 (중복 방지)
    boolean existsByOrderIdAndNotificationTypeAndNotifiedDate(
            Long orderId,
            NotificationType notificationType,
            LocalDate notifiedDate
    );

    // 전체 알림 최신순 조회
    List<Notification> findAllByOrderByNotifiedAtDesc();

    // 읽지 않은 알림 조회
    List<Notification> findByReadFalseOrderByNotifiedAtDesc();

    // 특정 발주의 알림 이력
    List<Notification> findByOrderIdOrderByNotifiedAtDesc(Long orderId);

    // 특정 날짜의 알림 조회
    List<Notification> findByNotifiedDate(LocalDate date);

    // 읽지 않은 알림 수 (count 쿼리 — 엔티티 전체 로드 없이 카운트)
    long countByReadFalse();
}
