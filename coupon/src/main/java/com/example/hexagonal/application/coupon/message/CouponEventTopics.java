package com.example.hexagonal.application.coupon.message;

public final class CouponEventTopics {
    public static final String COUPON_APPLY_COMMAND_V1 = "coupon.apply.command.v1";
    public static final String COUPON_APPLY_RESULT_V1 = "coupon.apply.result.v1";

    private CouponEventTopics() {
    }

    public static String messageKey(String orderId) {
        return orderId;
    }
}
