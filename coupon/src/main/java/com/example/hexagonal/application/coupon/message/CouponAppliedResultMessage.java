package com.example.hexagonal.application.coupon.message;

public record CouponAppliedResultMessage(
        String orderId,
        String benefitRequestId,
        String couponCode,
        long discountAmount,
        CouponResultCode resultCode
) {
}
