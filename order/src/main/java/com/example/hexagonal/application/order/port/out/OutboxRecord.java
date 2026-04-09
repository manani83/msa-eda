package com.example.hexagonal.application.order.port.out;

import java.time.Instant;

public record OutboxRecord(
        Long id,
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String topic,
        String messageKey,
        String correlationId,
        int schemaVersion,
        String payloadJson,
        OutboxStatus status,
        int retryCount,
        Instant nextAttemptAt,
        Instant publishedAt,
        Instant createdAt,
        String lastErrorMessage
) {
}
