package com.example.hexagonal.application.coupon.message;

public record CouponRejectedResultMessage(
        String orderId,
        String benefitRequestId,
        String couponCode,
        CouponResultCode resultCode,
        CouponFailureReason reasonCode,
        String reasonMessage
) {
}
