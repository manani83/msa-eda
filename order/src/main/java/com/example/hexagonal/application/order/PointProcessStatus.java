package com.example.hexagonal.application.order;

public enum PointProcessStatus {
    NOT_REQUESTED("NOT_REQUESTED", "포인트 미요청"),
    REQUESTED("REQUESTED", "포인트 요청됨"),
    RESERVED("RESERVED", "포인트 예약 완료"),
    FAILED("FAILED", "포인트 예약 실패");

    private final String code;
    private final String description;

    PointProcessStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PointProcessStatus fromCode(String code) {
        for (PointProcessStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown PointProcessStatus code: " + code);
    }
}
