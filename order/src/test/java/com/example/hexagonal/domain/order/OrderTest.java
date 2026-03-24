package com.example.hexagonal.domain.order;

import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.DiscountType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void create_whenCurrentSynchronousFlow_thenStatusIsBenefitsCompleted() {
        Instant createdAt = Instant.parse("2026-03-24T00:00:00Z");
        Coupon coupon = new Coupon(
                "WELCOME10",
                DiscountType.PERCENT,
                10,
                0,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T23:59:59Z")
        );

        Order order = Order.create(
                "user-1",
                List.of(new OrderItem("prod-1", 2, Money.of(1000))),
                new Address("12345", "line1", "line2"),
                coupon,
                createdAt
        );

        assertThat(order.getStatus()).isEqualTo(OrderStatus.BENEFITS_COMPLETED);
        assertThat(order.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(order.getDiscountAmount().getAmount()).isEqualTo(200);
        assertThat(order.getTotalAmount().getAmount()).isEqualTo(1800);
    }

    @Test
    void createPendingBenefits_whenBenefitCalculationDeferred_thenStatusIsPendingBenefits() {
        Instant createdAt = Instant.parse("2026-03-24T00:00:00Z");

        Order order = Order.createPendingBenefits(
                "user-1",
                List.of(new OrderItem("prod-1", 2, Money.of(1000))),
                new Address("12345", "line1", "line2"),
                "WELCOME10",
                createdAt
        );

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(order.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(order.getDiscountAmount().getAmount()).isZero();
        assertThat(order.getTotalAmount().getAmount()).isEqualTo(2000);
    }

    @Test
    void orderStatus_containsBusinessDescription() {
        assertThat(OrderStatus.PENDING_BENEFITS.getCode()).isEqualTo("PENDING_BENEFITS");
        assertThat(OrderStatus.PENDING_BENEFITS.getDescription()).isEqualTo("혜택 처리 대기");
        assertThat(OrderStatus.BENEFITS_COMPLETED.getCode()).isEqualTo("BENEFITS_COMPLETED");
        assertThat(OrderStatus.BENEFITS_COMPLETED.getDescription()).isEqualTo("혜택 처리 완료");
    }

    @Test
    void fromCode_whenKnownCode_thenReturnsMatchingStatus() {
        assertThat(OrderStatus.fromCode("PENDING_BENEFITS")).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(OrderStatus.fromCode("BENEFITS_COMPLETED")).isEqualTo(OrderStatus.BENEFITS_COMPLETED);
    }
}
