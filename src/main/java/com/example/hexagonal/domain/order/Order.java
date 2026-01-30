package com.example.hexagonal.domain.order;

import java.time.Instant;
import java.util.List;

public class Order {
    private final String orderId;
    private final String userId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final OrderStatus status;
    private final Money totalAmount;
    private final Instant createdAt;

    private Order(String orderId,
                  String userId,
                  List<OrderItem> items,
                  Address shippingAddress,
                  OrderStatus status,
                  Money totalAmount,
                  Instant createdAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("shippingAddress must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        this.orderId = orderId;
        this.userId = userId;
        this.items = List.copyOf(items);
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public static Order create(String userId, List<OrderItem> items, Address shippingAddress) {
        Money total = Money.of(0);
        for (OrderItem item : items) {
            total = total.add(item.lineTotal());
        }
        return new Order(null, userId, items, shippingAddress, OrderStatus.CREATED, total, Instant.now());
    }

    public static Order rehydrate(String id,
                                  String userId,
                                  List<OrderItem> items,
                                  Address shippingAddress,
                                  OrderStatus status,
                                  Money totalAmount,
                                  Instant createdAt) {
        return new Order(id, userId, items, shippingAddress, status, totalAmount, createdAt);
    }

    public Order withOrderId(String orderId) {
        return new Order(orderId, userId, items, shippingAddress, status, totalAmount, createdAt);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
