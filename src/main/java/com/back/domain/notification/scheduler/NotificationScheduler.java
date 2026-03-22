package com.back.domain.notification.scheduler;

import com.back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * 매일 오전 9시 — 지연/위험 발주 알림 자동 생성
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleDelayNotification() {
        log.info("[스케줄러] 납기 알림 생성 시작 - {}", LocalDateTime.now());
        try {
            int count = notificationService.generateDelayNotifications();
            log.info("[스케줄러] 납기 알림 생성 완료 - {}건", count);
        } catch (Exception e) {
            log.error("[스케줄러] 납기 알림 생성 중 오류 발생", e);
        }
    }
}
