package com.example.hexagonal.application.coupon.port.out;

import com.example.hexagonal.application.coupon.message.CouponFailureReason;

public interface CouponResultPublishPort {
    void publishApplied(String orderId, String benefitRequestId, String couponCode, long discountAmount);

    void publishRejected(String orderId,
                         String benefitRequestId,
                         String couponCode,
                         CouponFailureReason reason,
                         String reasonMessage);
}
