package com.example.hexagonal.application.order.port.out;

import java.time.Instant;

public interface OutboxCommandPort {
    void save(OutboxMessage outboxMessage);

    void markPublished(Long id, Instant publishedAt);

    void markFailed(Long id, Instant nextAttemptAt, String lastErrorMessage);
}
