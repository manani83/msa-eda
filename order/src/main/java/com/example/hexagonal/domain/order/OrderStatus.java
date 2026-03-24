package com.example.hexagonal.domain.order;

import java.util.Arrays;

public enum OrderStatus {
    PENDING_BENEFITS("PENDING_BENEFITS", "혜택 처리 대기"),
    BENEFITS_COMPLETED("BENEFITS_COMPLETED", "혜택 처리 완료"),
    BENEFITS_FAILED("BENEFITS_FAILED", "혜택 처리 실패"),
    PARTIAL_FAILED("PARTIAL_FAILED", "혜택 일부 처리 실패"),
    CANCELED("CANCELED", "주문 취소"),
    PAID("PAID", "결제 완료");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown OrderStatus code: " + code));
    }
}
