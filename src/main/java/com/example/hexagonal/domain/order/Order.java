package com.example.hexagonal.domain.order;

import com.example.hexagonal.domain.coupon.Coupon;

import java.time.Instant;
import java.util.List;

public class Order {
    private final String orderId;
    private final String userId;
    private final List<OrderItem> items;
    private final Address shippingAddress;
    private final OrderStatus status;
    private final String couponCode;
    private final Money discountAmount;
    private final Money totalAmount;
    private final Instant createdAt;

    private Order(String orderId,
                  String userId,
                  List<OrderItem> items,
                  Address shippingAddress,
                  OrderStatus status,
                  String couponCode,
                  Money discountAmount,
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
        if (discountAmount == null) {
            throw new IllegalArgumentException("discountAmount must not be null");
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
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public static Order create(String userId,
                               List<OrderItem> items,
                               Address shippingAddress,
                               Coupon coupon,
                               Instant createdAt) {
        Money subtotal = Money.of(0);
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.lineTotal());
        }
        long discount = coupon == null ? 0 : coupon.calculateDiscount(subtotal.getAmount(), createdAt);
        Money discountAmount = Money.of(discount);
        Money total = Money.of(subtotal.getAmount() - discount);
        String couponCode = coupon == null ? null : coupon.getCode();
        return new Order(null, userId, items, shippingAddress, OrderStatus.CREATED, couponCode, discountAmount, total, createdAt);
    }

    public static Order rehydrate(String id,
                                  String userId,
                                  List<OrderItem> items,
                                  Address shippingAddress,
                                  OrderStatus status,
                                  String couponCode,
                                  Money discountAmount,
                                  Money totalAmount,
                                  Instant createdAt) {
        return new Order(id, userId, items, shippingAddress, status, couponCode, discountAmount, totalAmount, createdAt);
    }

    public Order withOrderId(String orderId) {
        return new Order(orderId, userId, items, shippingAddress, status, couponCode, discountAmount, totalAmount, createdAt);
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

    public String getCouponCode() {
        return couponCode;
    }

    public Money getDiscountAmount() {
        return discountAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
