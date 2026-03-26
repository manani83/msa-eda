package com.example.hexagonal.application.event.coupon;

public record CouponAppliedEvent(
        String orderId,
        String benefitRequestId,
        String couponCode,
        long discountAmount,
        CouponResultCode resultCode
) {
}
