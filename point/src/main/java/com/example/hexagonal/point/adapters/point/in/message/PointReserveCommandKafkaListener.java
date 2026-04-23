package com.example.hexagonal.point.adapters.point.in.message;

import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.point.PointReserveCommandEvent;
import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.example.hexagonal.point.application.port.in.ReservePointCommand;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 주문이 발행한 포인트 예약 command 이벤트를 받아 Biz로 넘긴다.
 */
@Component
@ConditionalOnProperty(name = "point.command.listener.enabled", havingValue = "true")
public class PointReserveCommandKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(PointReserveCommandKafkaListener.class);
    private static final String SUPPORTED_EVENT_TYPE = "point.reserve.command";

    private final PointReserveCommandBiz pointReserveCommandBiz;
    private final ObjectMapper objectMapper;
    private final JavaType envelopeType;

    public PointReserveCommandKafkaListener(PointReserveCommandBiz pointReserveCommandBiz, ObjectMapper objectMapper) {
        this.pointReserveCommandBiz = pointReserveCommandBiz;
        this.objectMapper = objectMapper;
        this.envelopeType = objectMapper.getTypeFactory()
                .constructParametricType(EventEnvelope.class, PointReserveCommandEvent.class);
    }

    /**
     * Kafka 메시지를 읽어 포인트 예약 유스케이스로 전달한다.
     */
    @KafkaListener(
            topics = OrderEventTopics.POINT_RESERVE_COMMAND_V1,
            groupId = "${point.command.listener.group-id:point-command}"
    )
    public void listen(String message) throws IOException {
        EventEnvelope<PointReserveCommandEvent> envelope = objectMapper.readValue(message, envelopeType);
        if (!SUPPORTED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new IllegalArgumentException("Unsupported point event type: " + envelope.eventType());
        }
        log.info(
                "Kafka receive topic={} eventType={} correlationId={} traceId={} payload={}",
                OrderEventTopics.POINT_RESERVE_COMMAND_V1,
                envelope.eventType(),
                envelope.correlationId(),
                envelope.traceId(),
                envelope.payload()
        );
        pointReserveCommandBiz.reserve(new ReservePointCommand(
                envelope.payload().orderId(),
                envelope.payload().benefitRequestId(),
                envelope.payload().userId(),
                envelope.payload().requestedPointAmount(),
                envelope.payload().payableAmount(),
                envelope.payload().createdAt()
        ));
    }
}
