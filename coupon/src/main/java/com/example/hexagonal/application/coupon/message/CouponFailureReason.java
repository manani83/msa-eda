package com.example.hexagonal.application.coupon.message;

import java.util.Map;

public enum CouponFailureReason {
    NOT_FOUND("NOT_FOUND", "쿠폰을 찾을 수 없음"),
    EXPIRED("EXPIRED", "쿠폰 사용 기간 만료"),
    BELOW_MIN_ORDER_AMOUNT("BELOW_MIN_ORDER_AMOUNT", "최소 주문 금액 미만"),
    INVALID_STATE("INVALID_STATE", "잘못된 쿠폰 상태"),
    INTERNAL_ERROR("INTERNAL_ERROR", "내부 처리 오류");

    private static final Map<String, CouponFailureReason> BY_CODE = Map.of(
            NOT_FOUND.code, NOT_FOUND,
            EXPIRED.code, EXPIRED,
            BELOW_MIN_ORDER_AMOUNT.code, BELOW_MIN_ORDER_AMOUNT,
            INVALID_STATE.code, INVALID_STATE,
            INTERNAL_ERROR.code, INTERNAL_ERROR
    );

    private final String code;
    private final String description;

    CouponFailureReason(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CouponFailureReason fromCode(String code) {
        CouponFailureReason reason = BY_CODE.get(code);
        if (reason == null) {
            throw new IllegalArgumentException("Unknown CouponFailureReason code: " + code);
        }
        return reason;
    }
}
