package com.back.domain.order.order.enums;

public enum OrderStatus {

    NOT_STARTED("미착수"),
    PROCESSING("가공중"),
    COMPLETED("완료"),
    RECEIVED("입고");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
