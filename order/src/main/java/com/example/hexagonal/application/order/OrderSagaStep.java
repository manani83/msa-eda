package com.example.hexagonal.application.order;

public enum OrderSagaStep {
    WAITING_COUPON_RESULT("WAITING_COUPON_RESULT", "쿠폰 결과 대기"),
    WAITING_POINT_RESULT("WAITING_POINT_RESULT", "포인트 결과 대기"),
    COMPLETED("COMPLETED", "사가 완료"),
    FAILED("FAILED", "사가 실패");

    private final String code;
    private final String description;

    OrderSagaStep(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static OrderSagaStep fromCode(String code) {
        for (OrderSagaStep value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown OrderSagaStep code: " + code);
    }
}
