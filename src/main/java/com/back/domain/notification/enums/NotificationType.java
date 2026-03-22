package com.back.domain.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    WARNING("위험", "납기일이 3일 이내입니다."),
    DELAYED("지연", "납기일이 초과되었습니다.");

    private final String description;
    private final String defaultMessage;
}
