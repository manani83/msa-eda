package com.example.hexagonal.application.event.coupon;

public record CouponRejectedEvent(
        String orderId,
        String benefitRequestId,
        String couponCode,
        CouponResultCode resultCode,
        CouponFailureReason reasonCode,
        String reasonMessage
) {
}
