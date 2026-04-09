package com.example.hexagonal.application.event;

import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.coupon.CouponApplyCommandEvent;
import com.example.hexagonal.application.event.coupon.CouponFailureReason;
import com.example.hexagonal.application.event.coupon.CouponResultCode;
import com.example.hexagonal.application.event.order.OrderCreatedEvent;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.point.PointReserveCommandEvent;
import com.example.hexagonal.application.event.point.PointResultCode;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventSchemaPackageLayoutTest {

    @Test
    void eventEnvelope_whenWrappingOrderCreatedEvent_thenRetainsMetadataAndPayload() {
        OrderCreatedEvent payload = new OrderCreatedEvent(
                "20260324000001",
                "benefit-20260324000001",
                "user-1",
                "PENDING_BENEFITS",
                "WELCOME10",
                3000L,
                20000L,
                Instant.parse("2026-03-24T14:00:00Z"),
                List.of(new OrderCreatedEvent.OrderCreatedItem("prod-1", 2, 10000L))
        );

        EventEnvelope<OrderCreatedEvent> envelope = new EventEnvelope<>(
                "evt-order-created-001",
                "order.created",
                1,
                Instant.parse("2026-03-24T14:00:00Z"),
                "order-service",
                "order",
                "20260324000001",
                "benefit-20260324000001",
                "trace-123",
                payload
        );

        assertThat(envelope.aggregateId()).isEqualTo("20260324000001");
        assertThat(envelope.correlationId()).isEqualTo("benefit-20260324000001");
        assertThat(envelope.payload().orderStatusCode()).isEqualTo("PENDING_BENEFITS");
        assertThat(envelope.payload().items()).hasSize(1);
    }

    @Test
    void orderEventTopics_whenUsingOrderId_thenMessageKeyMatchesOrderId() {
        assertThat(OrderEventTopics.ORDER_CREATED_V1).isEqualTo("order.created.v1");
        assertThat(OrderEventTopics.COUPON_APPLY_COMMAND_V1).isEqualTo("coupon.apply.command.v1");
        assertThat(OrderEventTopics.COUPON_APPLY_RESULT_V1).isEqualTo("coupon.apply.result.v1");
        assertThat(OrderEventTopics.POINT_RESERVE_COMMAND_V1).isEqualTo("point.reserve.command.v1");
        assertThat(OrderEventTopics.POINT_RESERVE_RESULT_V1).isEqualTo("point.reserve.result.v1");
        assertThat(OrderEventTopics.messageKey("20260324000001")).isEqualTo("20260324000001");
    }

    @Test
    void commandEvents_shouldCarryOrchestrationPayload() {
        CouponApplyCommandEvent couponCommand = new CouponApplyCommandEvent(
                "20260324000001",
                "benefit-20260324000001",
                "user-1",
                "WELCOME10",
                20000L,
                Instant.parse("2026-03-24T14:00:00Z")
        );
        PointReserveCommandEvent pointCommand = new PointReserveCommandEvent(
                "20260324000001",
                "benefit-20260324000001",
                "user-1",
                3000L,
                18000L,
                Instant.parse("2026-03-24T14:00:01Z")
        );

        assertThat(couponCommand.couponCode()).isEqualTo("WELCOME10");
        assertThat(couponCommand.subtotalAmount()).isEqualTo(20000L);
        assertThat(pointCommand.requestedPointAmount()).isEqualTo(3000L);
        assertThat(pointCommand.payableAmount()).isEqualTo(18000L);
    }

    @Test
    void failureReasonEnums_shouldExposeExpectedCodes() {
        assertThat(CouponFailureReason.EXPIRED.getCode()).isEqualTo("EXPIRED");
        assertThat(CouponFailureReason.EXPIRED.getDescription()).isEqualTo("쿠폰 사용 기간 만료");
        assertThat(PointFailureReason.INSUFFICIENT_POINTS.getCode()).isEqualTo("INSUFFICIENT_POINTS");
        assertThat(PointFailureReason.INSUFFICIENT_POINTS.getDescription()).isEqualTo("보유 적립금 부족");
        assertThat(CouponResultCode.APPLIED.getCode()).isEqualTo("APPLIED");
        assertThat(CouponResultCode.APPLIED.getDescription()).isEqualTo("쿠폰 적용 성공");
        assertThat(PointResultCode.RESERVED.getCode()).isEqualTo("RESERVED");
        assertThat(PointResultCode.RESERVED.getDescription()).isEqualTo("적립금 예약 성공");
    }

    @Test
    void enumCodes_whenUsingFromCode_thenReturnMatchingEnums() {
        assertThat(CouponResultCode.fromCode("APPLIED")).isEqualTo(CouponResultCode.APPLIED);
        assertThat(CouponFailureReason.fromCode("EXPIRED")).isEqualTo(CouponFailureReason.EXPIRED);
        assertThat(PointResultCode.fromCode("RESERVED")).isEqualTo(PointResultCode.RESERVED);
        assertThat(PointFailureReason.fromCode("INSUFFICIENT_POINTS")).isEqualTo(PointFailureReason.INSUFFICIENT_POINTS);
    }
}
