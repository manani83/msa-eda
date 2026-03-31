package com.example.hexagonal.adapters.order.out.persistence;

import com.example.hexagonal.application.order.port.out.OutboxCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxPersistenceAdapter implements OutboxCommandPort {
    private final OrderOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxPersistenceAdapter(OrderOutboxJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(OutboxMessage outboxMessage) {
        repository.save(toEntity(outboxMessage));
    }

    private OrderOutboxEntity toEntity(OutboxMessage outboxMessage) {
        OrderOutboxEntity entity = new OrderOutboxEntity();
        entity.setEventId(outboxMessage.eventId());
        entity.setAggregateType(outboxMessage.aggregateType());
        entity.setAggregateId(outboxMessage.aggregateId());
        entity.setEventType(outboxMessage.eventType());
        entity.setTopic(outboxMessage.topic());
        entity.setMessageKey(outboxMessage.messageKey());
        entity.setCorrelationId(outboxMessage.correlationId());
        entity.setSchemaVersion(outboxMessage.schemaVersion());
        entity.setPayloadJson(serialize(outboxMessage));
        entity.setStatus(outboxMessage.status().getCode());
        entity.setRetryCount(outboxMessage.retryCount());
        entity.setNextAttemptAt(outboxMessage.nextAttemptAt());
        entity.setPublishedAt(outboxMessage.publishedAt());
        entity.setCreatedAt(outboxMessage.createdAt());
        entity.setLastErrorMessage(outboxMessage.lastErrorMessage());
        return entity;
    }

    private String serialize(OutboxMessage outboxMessage) {
        try {
            return objectMapper.writeValueAsString(outboxMessage.payload());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload: " + outboxMessage.eventType(), e);
        }
    }
}
