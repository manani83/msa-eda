package com.example.hexagonal.point.adapters.point.out.message;

import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.point.PointFailedEvent;
import com.example.hexagonal.application.event.point.PointReservedEvent;
import com.example.hexagonal.application.event.point.PointResultCode;
import com.example.hexagonal.point.application.port.out.PointResultPublishPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * 포인트 예약 결과를 Kafka result topic으로 발행한다.
 */
@Component
public class PointResultKafkaPublisher implements PointResultPublishPort {
    private static final String PRODUCER = "point-service";
    private static final String AGGREGATE_TYPE = "order";
    private static final int SCHEMA_VERSION = 1;
    private static final String POINT_RESERVED_EVENT_TYPE = "point.reserved";
    private static final String POINT_FAILED_EVENT_TYPE = "point.failed";

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PointResultKafkaPublisher(ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 포인트 예약 성공 결과를 result 이벤트로 보낸다.
     */
    @Override
    public void publishReserved(String orderId,
                                String benefitRequestId,
                                String userId,
                                long requestedPointAmount,
                                long reservedPointAmount) {
        PointReservedEvent payload = new PointReservedEvent(
                orderId,
                benefitRequestId,
                userId,
                requestedPointAmount,
                reservedPointAmount,
                PointResultCode.RESERVED
        );
        publish(orderId, envelope(POINT_RESERVED_EVENT_TYPE, orderId, benefitRequestId, payload));
    }

    /**
     * 포인트 예약 실패 결과를 result 이벤트로 보낸다.
     */
    @Override
    public void publishFailed(String orderId,
                              String benefitRequestId,
                              String userId,
                              long requestedPointAmount,
                              PointFailureReason reason,
                              String reasonMessage) {
        PointFailedEvent payload = new PointFailedEvent(
                orderId,
                benefitRequestId,
                userId,
                requestedPointAmount,
                PointResultCode.FAILED,
                reason,
                reasonMessage
        );
        publish(orderId, envelope(POINT_FAILED_EVENT_TYPE, orderId, benefitRequestId, payload));
    }

    /**
     * 공통 Kafka 전송 로직을 수행한다.
     */
    private void publish(String orderId, EventEnvelope<?> envelope) {
        try {
            kafkaTemplate.send(
                    OrderEventTopics.POINT_RESERVE_RESULT_V1,
                    OrderEventTopics.messageKey(orderId),
                    objectMapper.writeValueAsString(envelope)
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize point result event", exception);
        }
    }

    /**
     * 포인트 result 이벤트 공통 envelope를 만든다.
     */
    private <T> EventEnvelope<T> envelope(String eventType,
                                          String orderId,
                                          String benefitRequestId,
                                          T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER,
                AGGREGATE_TYPE,
                orderId,
                benefitRequestId,
                orderId,
                payload
        );
    }
}
