package com.example.hexagonal.application.coupon.message;

import java.time.Instant;

public record CouponApplyCommandMessage(
        String orderId,
        String benefitRequestId,
        String userId,
        String couponCode,
        long subtotalAmount,
        Instant createdAt
) {
}
