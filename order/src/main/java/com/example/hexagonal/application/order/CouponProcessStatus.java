package com.example.hexagonal.application.order;

public enum CouponProcessStatus {
    NOT_REQUESTED("NOT_REQUESTED", "쿠폰 미요청"),
    REQUESTED("REQUESTED", "쿠폰 요청됨"),
    APPLIED("APPLIED", "쿠폰 적용 완료"),
    REJECTED("REJECTED", "쿠폰 적용 실패");

    private final String code;
    private final String description;

    CouponProcessStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CouponProcessStatus fromCode(String code) {
        for (CouponProcessStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown CouponProcessStatus code: " + code);
    }
}
