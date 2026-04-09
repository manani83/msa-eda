package com.example.hexagonal.application.event.coupon;

import java.time.Instant;

public record CouponApplyCommandEvent(
        String orderId,
        String benefitRequestId,
        String userId,
        String couponCode,
        long subtotalAmount,
        Instant createdAt
) {
}
