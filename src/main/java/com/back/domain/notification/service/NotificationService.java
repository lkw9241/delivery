package com.back.domain.notification.service;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.order.order.entity.Order;
import com.back.domain.order.order.enums.RiskStatus;
import com.back.domain.order.order.repository.OrderRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.util.RiskEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;
    private final RiskEvaluator riskEvaluator;

    // 지연/위험 발주 스캔 후 알림 생성
    @Transactional
    public int generateDelayNotifications() {
        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findAll();
        List<Notification> newNotifications = new ArrayList<>();

        for (Order order : orders) {
            if (order.isReceived()) continue;

            RiskStatus risk = riskEvaluator.calculateRisk(order, today);

            if (risk == RiskStatus.DELAYED) {
                createIfNotExists(order, NotificationType.DELAYED, today, newNotifications);
            } else if (risk == RiskStatus.WARNING) {
                createIfNotExists(order, NotificationType.WARNING, today, newNotifications);
            }
        }

        if (!newNotifications.isEmpty()) {
            notificationRepository.saveAll(newNotifications);
            log.info("[알림] {}건 생성 완료 ({})", newNotifications.size(), today);
        } else {
            log.info("[알림] 새로 생성된 알림 없음 ({})", today);
        }

        return newNotifications.size();
    }

    // 전체 알림 최신순 조회
    public List<NotificationResponse> findAll() {
        return notificationRepository.findAllByOrderByNotifiedAtDesc()
                .stream().map(NotificationResponse::from).toList();
    }

    // 읽지 않은 알림만 조회
    public List<NotificationResponse> findUnread() {
        return notificationRepository.findByReadFalseOrderByNotifiedAtDesc()
                .stream().map(NotificationResponse::from).toList();
    }

    // 특정 발주의 알림 이력 조회
    public List<NotificationResponse> findByOrderId(Long orderId) {
        return notificationRepository.findByOrderIdOrderByNotifiedAtDesc(orderId)
                .stream().map(NotificationResponse::from).toList();
    }

    // 읽지 않은 알림 수 (count 쿼리)
    public long countUnread() {
        return notificationRepository.countByReadFalse();
    }

    // 단건 읽음 처리
    @Transactional
    public void markRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND,
                        "알림 ID [" + notificationId + "]를 찾을 수 없습니다."));
        notification.markRead();
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllRead() {
        List<Notification> unread = notificationRepository.findByReadFalseOrderByNotifiedAtDesc();
        unread.forEach(Notification::markRead);
        log.info("[알림] 전체 읽음 처리 {}건", unread.size());
    }

    // ───────────────────────────────────────
    // 내부 헬퍼
    // ───────────────────────────────────────
    private void createIfNotExists(Order order, NotificationType type,
                                   LocalDate today, List<Notification> target) {
        boolean alreadyExists = notificationRepository
                .existsByOrderIdAndNotificationTypeAndNotifiedDate(order.getId(), type, today);
        if (!alreadyExists) {
            target.add(Notification.create(
                    order.getId(), order.getPoNumber(), order.getVendor(),
                    order.getItem(), order.getDueDate(), type));
        }
    }
}
