package com.example.hexagonal.adapters.order.in.message;

import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.point.PointFailedEvent;
import com.example.hexagonal.application.event.point.PointReservedEvent;
import com.example.hexagonal.application.order.OrderSagaOrchestrator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "order.saga.listener.enabled", havingValue = "true")
public class PointResultKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(PointResultKafkaListener.class);
    private final OrderSagaOrchestrator orderSagaOrchestrator;
    private final ObjectMapper objectMapper;

    public PointResultKafkaListener(OrderSagaOrchestrator orderSagaOrchestrator, ObjectMapper objectMapper) {
        this.orderSagaOrchestrator = orderSagaOrchestrator;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = OrderEventTopics.POINT_RESERVE_RESULT_V1,
            groupId = "${order.saga.listener.group-id:order-saga}"
    )
    // 포인트 결과 이벤트를 읽어 오케스트레이터에 전달한다.
    public void listen(String message) throws IOException {
        JsonNode root = objectMapper.readTree(message);
        String eventType = text(root, "eventType");
        if ("point.reserved".equals(eventType)) {
            EventEnvelope<PointReservedEvent> envelope = envelope(root, PointReservedEvent.class);
            log.info(
                    "Kafka receive topic={} eventType={} correlationId={} traceId={} payload={}",
                    OrderEventTopics.POINT_RESERVE_RESULT_V1,
                    envelope.eventType(),
                    envelope.correlationId(),
                    envelope.traceId(),
                    envelope.payload()
            );
            orderSagaOrchestrator.handlePointReserved(envelope);
            return;
        }
        if ("point.failed".equals(eventType)) {
            EventEnvelope<PointFailedEvent> envelope = envelope(root, PointFailedEvent.class);
            log.info(
                    "Kafka receive topic={} eventType={} correlationId={} traceId={} payload={}",
                    OrderEventTopics.POINT_RESERVE_RESULT_V1,
                    envelope.eventType(),
                    envelope.correlationId(),
                    envelope.traceId(),
                    envelope.payload()
            );
            orderSagaOrchestrator.handlePointFailed(envelope);
            return;
        }
        throw new IllegalArgumentException("Unsupported point event type: " + eventType);
    }

    // Kafka JSON 메시지를 공통 envelope 객체로 복원한다.
    private <T> EventEnvelope<T> envelope(JsonNode root, Class<T> payloadType) throws IOException {
        return new EventEnvelope<>(
                text(root, "eventId"),
                text(root, "eventType"),
                root.get("schemaVersion").asInt(),
                Instant.parse(text(root, "occurredAt")),
                text(root, "producer"),
                text(root, "aggregateType"),
                text(root, "aggregateId"),
                text(root, "correlationId"),
                text(root, "traceId"),
                objectMapper.treeToValue(root.get("payload"), payloadType)
        );
    }

    // 공통 필드를 문자열로 읽기 위한 헬퍼다.
    private String text(JsonNode root, String fieldName) {
        return root.get(fieldName).asText();
    }
}
