package com.example.hexagonal.application.event.point;

import java.util.Arrays;

public enum PointFailureReason {
    INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", "보유 적립금 부족"),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "적립금 계정을 찾을 수 없음"),
    INVALID_AMOUNT("INVALID_AMOUNT", "잘못된 적립금 요청 금액"),
    INVALID_STATE("INVALID_STATE", "잘못된 적립금 상태"),
    INTERNAL_ERROR("INTERNAL_ERROR", "내부 처리 오류");

    private final String code;
    private final String description;

    PointFailureReason(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PointFailureReason fromCode(String code) {
        return Arrays.stream(values())
                .filter(reason -> reason.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown PointFailureReason code: " + code));
    }
}
