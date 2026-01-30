package com.example.hexagonal.adapters.order.in.web.dto;

import java.time.Instant;

public class CreateOrderResponse {
    private final String orderId;
    private final String status;
    private final long totalAmount;
    private final Instant createdAt;

    public CreateOrderResponse(String orderId, String status, long totalAmount, Instant createdAt) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
