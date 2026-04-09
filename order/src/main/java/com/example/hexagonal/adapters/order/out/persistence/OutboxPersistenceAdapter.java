package com.example.hexagonal.adapters.order.out.persistence;

import com.example.hexagonal.application.order.port.out.OutboxCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxMessage;
import com.example.hexagonal.application.order.port.out.OutboxQueryPort;
import com.example.hexagonal.application.order.port.out.OutboxRecord;
import com.example.hexagonal.application.order.port.out.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPersistenceAdapter implements OutboxCommandPort, OutboxQueryPort {
    private final OrderOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxPersistenceAdapter(OrderOutboxJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void save(OutboxMessage outboxMessage) {
        repository.save(toEntity(outboxMessage));
    }

    @Override
    public List<OutboxRecord> findPublishable(int limit, Instant now) {
        return repository.findPublishable(
                        List.of(OutboxStatus.PENDING.getCode(), OutboxStatus.FAILED.getCode()),
                        now,
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long id, Instant publishedAt) {
        OrderOutboxEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outbox row not found: " + id));
        entity.setStatus(OutboxStatus.PUBLISHED.getCode());
        entity.setPublishedAt(publishedAt);
        entity.setLastErrorMessage(null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id, Instant nextAttemptAt, String lastErrorMessage) {
        OrderOutboxEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outbox row not found: " + id));
        entity.setStatus(OutboxStatus.FAILED.getCode());
        entity.setRetryCount(entity.getRetryCount() + 1);
        entity.setNextAttemptAt(nextAttemptAt);
        entity.setLastErrorMessage(truncate(lastErrorMessage, 1000));
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

    private OutboxRecord toRecord(OrderOutboxEntity entity) {
        return new OutboxRecord(
                entity.getId(),
                entity.getEventId(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getEventType(),
                entity.getTopic(),
                entity.getMessageKey(),
                entity.getCorrelationId(),
                entity.getSchemaVersion(),
                entity.getPayloadJson(),
                OutboxStatus.fromCode(entity.getStatus()),
                entity.getRetryCount(),
                entity.getNextAttemptAt(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getLastErrorMessage()
        );
    }

    private String serialize(OutboxMessage outboxMessage) {
        try {
            return objectMapper.writeValueAsString(outboxMessage.payload());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload: " + outboxMessage.eventType(), e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
