package com.example.hexagonal.application.event.common;

import java.time.Instant;

public record EventEnvelope<T>(
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
