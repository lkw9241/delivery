package com.back.domain.notification.controller;

import com.back.domain.notification.dto.response.NotificationResponse;
import com.back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // ───────────────────────────────────────
    // 알림 목록 전체 페이지
    // ───────────────────────────────────────
    @GetMapping
    public String list(Model model) {
        List<NotificationResponse> notifications = notificationService.findAll();
        long unreadCount = notificationService.countUnread();

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "notifications";
    }

    // ───────────────────────────────────────
    // 단건 읽음 처리 → 알림 목록으로 리다이렉트
    // ───────────────────────────────────────
    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return "redirect:/notifications";
    }

    // ───────────────────────────────────────
    // 전체 읽음 처리 → 알림 목록으로 리다이렉트
    // ───────────────────────────────────────
    @PostMapping("/read-all")
    public String markAllRead() {
        notificationService.markAllRead();
        return "redirect:/notifications";
    }
}
