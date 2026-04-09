package com.example.hexagonal.adapters.coupon.in.message;

import com.example.hexagonal.application.coupon.CouponApplyCommandBiz;
import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CouponApplyCommandKafkaListenerTest {

    private CouponApplyCommandBiz couponApplyCommandBiz;
    private CouponApplyCommandKafkaListener couponApplyCommandKafkaListener;

    @BeforeEach
    void setUp() {
        couponApplyCommandBiz = mock(CouponApplyCommandBiz.class);
        couponApplyCommandKafkaListener = new CouponApplyCommandKafkaListener(couponApplyCommandBiz, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void listen_whenCouponApplyCommandMessageArrives_thenDelegatesToBiz() throws Exception {
        String message = """
                {
                  "eventId":"evt-1",
                  "eventType":"coupon.apply.command",
                  "schemaVersion":1,
                  "occurredAt":"2026-04-03T10:00:00Z",
                  "producer":"order-service",
                  "aggregateType":"order",
                  "aggregateId":"order-1",
                  "correlationId":"benefit-order-1",
                  "traceId":"order-1",
                  "payload":{
                    "orderId":"order-1",
                    "benefitRequestId":"benefit-order-1",
                    "userId":"user-1",
                    "couponCode":"WELCOME10",
                    "subtotalAmount":10000,
                    "createdAt":"2026-04-03T10:00:00Z"
                  }
                }
                """;

        couponApplyCommandKafkaListener.listen(message);

        verify(couponApplyCommandBiz).apply(argThat(command ->
                sameCommand(command, "order-1", "benefit-order-1", "WELCOME10", 10_000L, Instant.parse("2026-04-03T10:00:00Z"))
        ));
    }

    @Test
    void listen_whenUnsupportedEventTypeArrives_thenThrowsException() {
        String message = """
                {
                  "eventId":"evt-1",
                  "eventType":"coupon.unknown",
                  "schemaVersion":1,
                  "occurredAt":"2026-04-03T10:00:00Z",
                  "producer":"order-service",
                  "aggregateType":"order",
                  "aggregateId":"order-1",
                  "correlationId":"benefit-order-1",
                  "traceId":"order-1",
                  "payload":{
                    "orderId":"order-1",
                    "benefitRequestId":"benefit-order-1",
                    "userId":"user-1",
                    "couponCode":"WELCOME10",
                    "subtotalAmount":10000,
                    "createdAt":"2026-04-03T10:00:00Z"
                  }
                }
                """;

        assertThatThrownBy(() -> couponApplyCommandKafkaListener.listen(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported coupon event type");
    }

    private boolean sameCommand(ApplyCouponCommand command,
                                String orderId,
                                String benefitRequestId,
                                String couponCode,
                                long subtotalAmount,
                                Instant createdAt) {
        return orderId.equals(command.orderId())
                && benefitRequestId.equals(command.benefitRequestId())
                && couponCode.equals(command.couponCode())
                && subtotalAmount == command.subtotalAmount()
                && createdAt.equals(command.createdAt());
    }
}
