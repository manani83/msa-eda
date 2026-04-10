package com.example.hexagonal.point.adapters.point.in.message;

import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.example.hexagonal.point.application.port.in.ReservePointCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 포인트 command Kafka 리스너가 JSON 메시지를 Biz 호출로 연결하는지 검증한다.
 */
class PointReserveCommandKafkaListenerTest {

    private PointReserveCommandBiz pointReserveCommandBiz;
    private PointReserveCommandKafkaListener pointReserveCommandKafkaListener;

    @BeforeEach
    void setUp() {
        pointReserveCommandBiz = mock(PointReserveCommandBiz.class);
        pointReserveCommandKafkaListener = new PointReserveCommandKafkaListener(
                pointReserveCommandBiz,
                new ObjectMapper().findAndRegisterModules()
        );
    }

    @Test
    void listen_whenPointReserveCommandArrives_thenDelegatesToBiz() throws Exception {
        String message = """
                {
                  "eventId":"evt-1",
                  "eventType":"point.reserve.command",
                  "schemaVersion":1,
                  "occurredAt":"2026-04-10T10:00:00Z",
                  "producer":"order-service",
                  "aggregateType":"order",
                  "aggregateId":"order-1",
                  "correlationId":"benefit-order-1",
                  "traceId":"order-1",
                  "payload":{
                    "orderId":"order-1",
                    "benefitRequestId":"benefit-order-1",
                    "userId":"user-1",
                    "requestedPointAmount":300,
                    "payableAmount":1700,
                    "createdAt":"2026-04-10T10:00:00Z"
                  }
                }
                """;

        pointReserveCommandKafkaListener.listen(message);

        verify(pointReserveCommandBiz).reserve(argThat(command ->
                sameCommand(command, "order-1", "benefit-order-1", "user-1", 300L, 1700L, Instant.parse("2026-04-10T10:00:00Z"))
        ));
    }

    @Test
    void listen_whenUnsupportedEventTypeArrives_thenThrowsException() {
        String message = """
                {
                  "eventId":"evt-1",
                  "eventType":"point.unknown",
                  "schemaVersion":1,
                  "occurredAt":"2026-04-10T10:00:00Z",
                  "producer":"order-service",
                  "aggregateType":"order",
                  "aggregateId":"order-1",
                  "correlationId":"benefit-order-1",
                  "traceId":"order-1",
                  "payload":{
                    "orderId":"order-1",
                    "benefitRequestId":"benefit-order-1",
                    "userId":"user-1",
                    "requestedPointAmount":300,
                    "payableAmount":1700,
                    "createdAt":"2026-04-10T10:00:00Z"
                  }
                }
                """;

        assertThatThrownBy(() -> pointReserveCommandKafkaListener.listen(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported point event type");
    }

    /**
     * 테스트 메시지가 기대한 명령으로 변환됐는지 비교한다.
     */
    private boolean sameCommand(ReservePointCommand command,
                                String orderId,
                                String benefitRequestId,
                                String userId,
                                long requestedPointAmount,
                                long payableAmount,
                                Instant createdAt) {
        return orderId.equals(command.orderId())
                && benefitRequestId.equals(command.benefitRequestId())
                && userId.equals(command.userId())
                && requestedPointAmount == command.requestedPointAmount()
                && payableAmount == command.payableAmount()
                && createdAt.equals(command.createdAt());
    }
}
