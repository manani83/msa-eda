package com.example.hexagonal.application.order.port.out;

public enum OutboxStatus {
    PENDING("PENDING", "발행 대기"),
    PUBLISHED("PUBLISHED", "발행 완료"),
    FAILED("FAILED", "발행 실패");

    private final String code;
    private final String description;

    OutboxStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OutboxStatus fromCode(String code) {
        for (OutboxStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown OutboxStatus code: " + code);
    }
}
