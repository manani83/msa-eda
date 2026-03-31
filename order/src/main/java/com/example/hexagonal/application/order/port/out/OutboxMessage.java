package com.example.hexagonal.application.order.port.out;

import com.example.hexagonal.application.event.common.EventEnvelope;

import java.time.Instant;

public record OutboxMessage(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String topic,
        String messageKey,
        String correlationId,
        int schemaVersion,
        EventEnvelope<?> payload,
        OutboxStatus status,
        int retryCount,
        Instant nextAttemptAt,
        Instant publishedAt,
        Instant createdAt,
        String lastErrorMessage
) {
    public static OutboxMessage pending(String topic, String messageKey, EventEnvelope<?> payload) {
        return new OutboxMessage(
                payload.eventId(),
                payload.aggregateType(),
                payload.aggregateId(),
                payload.eventType(),
                topic,
                messageKey,
                payload.correlationId(),
                payload.schemaVersion(),
                payload,
                OutboxStatus.PENDING,
                0,
                payload.occurredAt(),
                null,
                payload.occurredAt(),
                null
        );
    }
}
