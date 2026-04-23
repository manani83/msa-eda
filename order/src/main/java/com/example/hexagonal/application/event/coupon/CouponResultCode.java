package com.example.hexagonal.application.event.coupon;

import java.util.Map;

public enum CouponResultCode {
    APPLIED("APPLIED", "쿠폰 적용 성공"),
    REJECTED("REJECTED", "쿠폰 적용 실패");

    private static final Map<String, CouponResultCode> BY_CODE = Map.of(
            APPLIED.code, APPLIED,
            REJECTED.code, REJECTED
    );

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
        CouponResultCode resultCode = BY_CODE.get(code);
        if (resultCode == null) {
            throw new IllegalArgumentException("Unknown CouponResultCode code: " + code);
        }
        return resultCode;
    }
}
