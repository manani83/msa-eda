package com.example.hexagonal.application.event.point;

import java.util.Arrays;

public enum PointResultCode {
    RESERVED("RESERVED", "적립금 예약 성공"),
    FAILED("FAILED", "적립금 처리 실패");

    private final String code;
    private final String description;

    PointResultCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PointResultCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(resultCode -> resultCode.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown PointResultCode code: " + code));
    }
}
