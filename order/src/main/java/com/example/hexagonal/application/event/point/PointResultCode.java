package com.example.hexagonal.application.event.point;

import java.util.Map;

public enum PointResultCode {
    RESERVED("RESERVED", "적립금 예약 성공"),
    FAILED("FAILED", "적립금 처리 실패");

    private static final Map<String, PointResultCode> BY_CODE = Map.of(
            RESERVED.code, RESERVED,
            FAILED.code, FAILED
    );

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
        PointResultCode resultCode = BY_CODE.get(code);
        if (resultCode == null) {
            throw new IllegalArgumentException("Unknown PointResultCode code: " + code);
        }
        return resultCode;
    }
}
