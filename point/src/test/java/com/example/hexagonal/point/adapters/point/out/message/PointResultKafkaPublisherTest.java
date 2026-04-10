package com.example.hexagonal.point.adapters.point.out.message;

import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 포인트 result publisher가 올바른 Kafka envelope를 만드는지 검증한다.
 */
class PointResultKafkaPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private KafkaTemplate<String, String> kafkaTemplate;
    private PointResultKafkaPublisher pointResultKafkaPublisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);
        pointResultKafkaPublisher = new PointResultKafkaPublisher(objectMapper, kafkaTemplate);
    }

    @Test
    void publishReserved_whenCalled_thenSendsPointReservedEvent() throws Exception {
        pointResultKafkaPublisher.publishReserved("order-1", "benefit-order-1", "user-1", 300L, 300L);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(
                org.mockito.Mockito.eq(OrderEventTopics.POINT_RESERVE_RESULT_V1),
                org.mockito.Mockito.eq("order-1"),
                messageCaptor.capture()
        );

        JsonNode root = objectMapper.readTree(messageCaptor.getValue());
        assertThat(root.get("eventType").asText()).isEqualTo("point.reserved");
        assertThat(root.get("aggregateId").asText()).isEqualTo("order-1");
        assertThat(root.get("correlationId").asText()).isEqualTo("benefit-order-1");
        assertThat(root.get("payload").get("reservedPointAmount").asLong()).isEqualTo(300L);
        assertThat(root.get("payload").get("resultCode").asText()).isEqualTo("RESERVED");
    }

    @Test
    void publishFailed_whenCalled_thenSendsPointFailedEvent() throws Exception {
        pointResultKafkaPublisher.publishFailed(
                "order-1",
                "benefit-order-1",
                "user-1",
                300L,
                PointFailureReason.INSUFFICIENT_POINTS,
                "Not enough points"
        );

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(
                org.mockito.Mockito.eq(OrderEventTopics.POINT_RESERVE_RESULT_V1),
                org.mockito.Mockito.eq("order-1"),
                messageCaptor.capture()
        );

        JsonNode root = objectMapper.readTree(messageCaptor.getValue());
        assertThat(root.get("eventType").asText()).isEqualTo("point.failed");
        assertThat(root.get("payload").get("resultCode").asText()).isEqualTo("FAILED");
        assertThat(root.get("payload").get("reasonCode").asText()).isEqualTo("INSUFFICIENT_POINTS");
        assertThat(root.get("payload").get("reasonMessage").asText()).isEqualTo("Not enough points");
    }
}
