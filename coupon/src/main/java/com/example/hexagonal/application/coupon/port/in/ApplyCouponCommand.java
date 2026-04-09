package com.example.hexagonal.application.coupon.port.in;

import java.time.Instant;

public record ApplyCouponCommand(
        String orderId,
        String benefitRequestId,
        String couponCode,
        long subtotalAmount,
        Instant createdAt
) {
}
