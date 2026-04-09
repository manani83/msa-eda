package com.example.hexagonal.application.coupon.message;

import java.time.Instant;

public record CouponEventEnvelope<T>(
        String eventId,
        String eventType,
        int schemaVersion,
        Instant occurredAt,
        String producer,
        String aggregateType,
        String aggregateId,
        String correlationId,
        String traceId,
        T payload
) {
}
