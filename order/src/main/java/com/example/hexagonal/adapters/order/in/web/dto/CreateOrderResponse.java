package com.example.hexagonal.adapters.order.in.web.dto;

import java.time.Instant;

public class CreateOrderResponse {
    private final String orderId;
    private final String status;
    private final String couponCode;
    private final long discountAmount;
    private final long totalAmount;
    private final Instant createdAt;

    public CreateOrderResponse(String orderId,
                               String status,
                               String couponCode,
                               long discountAmount,
                               long totalAmount,
                               Instant createdAt) {
        this.orderId = orderId;
        this.status = status;
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
