package com.example.hexagonal.application.order.port.in;

import com.example.hexagonal.domain.order.OrderStatus;

import java.time.Instant;

public class CreateOrderResult {
    private final String orderId;
    private final OrderStatus status;
    private final String couponCode;
    private final long discountAmount;
    private final long totalAmount;
    private final Instant createdAt;

    public CreateOrderResult(String orderId,
                             OrderStatus status,
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

    public OrderStatus getStatus() {
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
