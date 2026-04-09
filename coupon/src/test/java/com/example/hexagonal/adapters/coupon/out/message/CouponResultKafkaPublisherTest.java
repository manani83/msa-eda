package com.example.hexagonal.adapters.coupon.out.message;

import com.example.hexagonal.application.coupon.message.CouponFailureReason;
import com.example.hexagonal.application.coupon.message.CouponEventTopics;
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

class CouponResultKafkaPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private KafkaTemplate<String, String> kafkaTemplate;
    private CouponResultKafkaPublisher couponResultKafkaPublisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);
        couponResultKafkaPublisher = new CouponResultKafkaPublisher(objectMapper, kafkaTemplate);
    }

    @Test
    void publishApplied_whenCalled_thenSendsCouponAppliedEvent() throws Exception {
        couponResultKafkaPublisher.publishApplied("order-1", "benefit-order-1", "WELCOME10", 1_000L);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(
                org.mockito.Mockito.eq(CouponEventTopics.COUPON_APPLY_RESULT_V1),
                org.mockito.Mockito.eq("order-1"),
                messageCaptor.capture()
        );

        JsonNode root = objectMapper.readTree(messageCaptor.getValue());
        assertThat(root.get("eventType").asText()).isEqualTo("coupon.applied");
        assertThat(root.get("aggregateId").asText()).isEqualTo("order-1");
        assertThat(root.get("correlationId").asText()).isEqualTo("benefit-order-1");
        assertThat(root.get("payload").get("discountAmount").asLong()).isEqualTo(1_000L);
        assertThat(root.get("payload").get("resultCode").asText()).isEqualTo("APPLIED");
    }

    @Test
    void publishRejected_whenCalled_thenSendsCouponRejectedEvent() throws Exception {
        couponResultKafkaPublisher.publishRejected(
                "order-1",
                "benefit-order-1",
                "WELCOME10",
                CouponFailureReason.EXPIRED,
                "Coupon expired"
        );

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(
                org.mockito.Mockito.eq(CouponEventTopics.COUPON_APPLY_RESULT_V1),
                org.mockito.Mockito.eq("order-1"),
                messageCaptor.capture()
        );

        JsonNode root = objectMapper.readTree(messageCaptor.getValue());
        assertThat(root.get("eventType").asText()).isEqualTo("coupon.rejected");
        assertThat(root.get("payload").get("resultCode").asText()).isEqualTo("REJECTED");
        assertThat(root.get("payload").get("reasonCode").asText()).isEqualTo("EXPIRED");
        assertThat(root.get("payload").get("reasonMessage").asText()).isEqualTo("Coupon expired");
    }
}
