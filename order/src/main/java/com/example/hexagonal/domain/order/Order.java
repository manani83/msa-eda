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
        Money subtotal = subtotalOf(items);
        long discount = coupon == null ? 0 : coupon.calculateDiscount(subtotal.getAmount(), createdAt);
        Money discountAmount = Money.of(discount);
        Money total = Money.of(subtotal.getAmount() - discount);
        String couponCode = coupon == null ? null : coupon.getCode();
        return new Order(
                null,
                userId,
                items,
                shippingAddress,
                OrderStatus.BENEFITS_COMPLETED,
                couponCode,
                discountAmount,
                total,
                createdAt
        );
    }

    public static Order createPendingBenefits(String userId,
                                              List<OrderItem> items,
                                              Address shippingAddress,
                                              String couponCode,
                                              Instant createdAt) {
        Money subtotal = subtotalOf(items);
        return new Order(
                null,
                userId,
                items,
                shippingAddress,
                OrderStatus.PENDING_BENEFITS,
                couponCode,
                Money.of(0),
                subtotal,
                createdAt
        );
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

    // 상품 합계를 기준으로 현재 주문의 원래 금액을 계산한다.
    public long subtotalAmount() {
        return subtotalOf(items).getAmount();
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

    // 혜택 처리가 모두 끝난 주문 상태로 전환한다.
    public Order completeBenefits() {
        return new Order(orderId, userId, items, shippingAddress, OrderStatus.BENEFITS_COMPLETED, couponCode, discountAmount, totalAmount, createdAt);
    }

    // 쿠폰 할인 결과를 반영해 주문 금액과 상태를 갱신한다.
    public Order applyCouponDiscount(long couponDiscountAmount, boolean keepPending) {
        long currentAmount = totalAmount.getAmount();
        return new Order(
                orderId,
                userId,
                items,
                shippingAddress,
                keepPending ? OrderStatus.PENDING_BENEFITS : OrderStatus.BENEFITS_COMPLETED,
                couponCode,
                Money.of(couponDiscountAmount),
                Money.of(currentAmount - couponDiscountAmount),
                createdAt
        );
    }

    // 포인트 예약 결과를 반영해 최종 할인 금액과 결제 금액을 갱신한다.
    public Order applyPointReservation(long reservedPointAmount) {
        return new Order(
                orderId,
                userId,
                items,
                shippingAddress,
                OrderStatus.BENEFITS_COMPLETED,
                couponCode,
                Money.of(discountAmount.getAmount() + reservedPointAmount),
                Money.of(totalAmount.getAmount() - reservedPointAmount),
                createdAt
        );
    }

    // 혜택 처리 전체 실패 상태로 전환한다.
    public Order markBenefitsFailed() {
        return new Order(orderId, userId, items, shippingAddress, OrderStatus.BENEFITS_FAILED, couponCode, discountAmount, totalAmount, createdAt);
    }

    // 일부 혜택만 적용된 실패 상태로 전환한다.
    public Order markPartialFailed() {
        return new Order(orderId, userId, items, shippingAddress, OrderStatus.PARTIAL_FAILED, couponCode, discountAmount, totalAmount, createdAt);
    }

    private static Money subtotalOf(List<OrderItem> items) {
        Money subtotal = Money.of(0);
        for (OrderItem item : items) {
            subtotal = subtotal.add(item.lineTotal());
        }
        return subtotal;
    }
}
