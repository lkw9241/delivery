package com.back.domain.order.order.enums;

public enum RiskStatus {

    NORMAL("정상"),
    WARNING("지연위험"),
    DELAYED("지연");

    private final String description;

    RiskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
