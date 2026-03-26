package com.example.hexagonal.application.event.coupon;

import java.util.Arrays;

public enum CouponResultCode {
    APPLIED("APPLIED", "쿠폰 적용 성공"),
    REJECTED("REJECTED", "쿠폰 적용 실패");

    private final String code;
    private final String description;

    CouponResultCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CouponResultCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(resultCode -> resultCode.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown CouponResultCode code: " + code));
    }
}
