package com.example.hexagonal.point.adapters.point.in.message;

import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.point.PointReserveCommandEvent;
import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.example.hexagonal.point.application.port.in.ReservePointCommand;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final PointReserveCommandBiz pointReserveCommandBiz;
    private final ObjectMapper objectMapper;

    public PointReserveCommandKafkaListener(PointReserveCommandBiz pointReserveCommandBiz, ObjectMapper objectMapper) {
        this.pointReserveCommandBiz = pointReserveCommandBiz;
        this.objectMapper = objectMapper;
    }

    /**
     * Kafka 메시지를 읽어 포인트 예약 유스케이스로 전달한다.
     */
    @KafkaListener(
            topics = OrderEventTopics.POINT_RESERVE_COMMAND_V1,
            groupId = "${point.command.listener.group-id:point-command}"
    )
    public void listen(String message) throws IOException {
        JsonNode root = objectMapper.readTree(message);
        String eventType = text(root, "eventType");
        if (!"point.reserve.command".equals(eventType)) {
            throw new IllegalArgumentException("Unsupported point event type: " + eventType);
        }

        PointReserveCommandEvent payload = objectMapper.treeToValue(root.get("payload"), PointReserveCommandEvent.class);
        log.info(
                "Kafka receive topic={} eventType={} correlationId={} traceId={} payload={}",
                OrderEventTopics.POINT_RESERVE_COMMAND_V1,
                eventType,
                text(root, "correlationId"),
                text(root, "traceId"),
                payload
        );
        pointReserveCommandBiz.reserve(new ReservePointCommand(
                payload.orderId(),
                payload.benefitRequestId(),
                payload.userId(),
                payload.requestedPointAmount(),
                payload.payableAmount(),
                payload.createdAt()
        ));
    }

    /**
     * JSON 필드에서 문자열 값을 읽는다.
     */
    private String text(JsonNode root, String fieldName) {
        return root.get(fieldName).asText();
    }
}
