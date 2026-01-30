package com.example.hexagonal.application.order.port.in;

import com.example.hexagonal.domain.order.OrderStatus;

import java.time.Instant;

public class CreateOrderResult {
    private final String orderId;
    private final OrderStatus status;
    private final long totalAmount;
    private final Instant createdAt;

    public CreateOrderResult(String orderId, OrderStatus status, long totalAmount, Instant createdAt) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
