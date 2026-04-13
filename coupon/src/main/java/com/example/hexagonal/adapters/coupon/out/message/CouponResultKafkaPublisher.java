package com.example.hexagonal.adapters.coupon.out.message;

import com.example.hexagonal.application.coupon.message.CouponAppliedResultMessage;
import com.example.hexagonal.application.coupon.message.CouponEventEnvelope;
import com.example.hexagonal.application.coupon.message.CouponEventTopics;
import com.example.hexagonal.application.coupon.message.CouponFailureReason;
import com.example.hexagonal.application.coupon.message.CouponRejectedResultMessage;
import com.example.hexagonal.application.coupon.message.CouponResultCode;
import com.example.hexagonal.application.coupon.port.out.CouponResultPublishPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CouponResultKafkaPublisher implements CouponResultPublishPort {
    private static final Logger log = LoggerFactory.getLogger(CouponResultKafkaPublisher.class);
    private static final String PRODUCER = "coupon-service";
    private static final String AGGREGATE_TYPE = "order";
    private static final int SCHEMA_VERSION = 1;
    private static final String COUPON_APPLIED_EVENT_TYPE = "coupon.applied";
    private static final String COUPON_REJECTED_EVENT_TYPE = "coupon.rejected";

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CouponResultKafkaPublisher(ObjectMapper objectMapper,
                                      KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    // 쿠폰 적용 성공 결과를 Kafka로 발행한다.
    public void publishApplied(String orderId, String benefitRequestId, String couponCode, long discountAmount) {
        CouponAppliedResultMessage payload = new CouponAppliedResultMessage(
                orderId,
                benefitRequestId,
                couponCode,
                discountAmount,
                CouponResultCode.APPLIED
        );
        publish(orderId, envelope(COUPON_APPLIED_EVENT_TYPE, orderId, benefitRequestId, payload));
    }

    @Override
    // 쿠폰 적용 실패 결과를 Kafka로 발행한다.
    public void publishRejected(String orderId,
                                String benefitRequestId,
                                String couponCode,
                                CouponFailureReason reason,
                                String reasonMessage) {
        CouponRejectedResultMessage payload = new CouponRejectedResultMessage(
                orderId,
                benefitRequestId,
                couponCode,
                CouponResultCode.REJECTED,
                reason,
                reasonMessage
        );
        publish(orderId, envelope(COUPON_REJECTED_EVENT_TYPE, orderId, benefitRequestId, payload));
    }

    // 공통 envelope를 직렬화해 result topic으로 보낸다.
    private void publish(String orderId, CouponEventEnvelope<?> envelope) {
        try {
            String message = objectMapper.writeValueAsString(envelope);
            log.info(
                    "Kafka send topic={} key={} eventType={} correlationId={} traceId={} payload={}",
                    CouponEventTopics.COUPON_APPLY_RESULT_V1,
                    CouponEventTopics.messageKey(orderId),
                    envelope.eventType(),
                    envelope.correlationId(),
                    envelope.traceId(),
                    message
            );
            kafkaTemplate.send(
                    CouponEventTopics.COUPON_APPLY_RESULT_V1,
                    CouponEventTopics.messageKey(orderId),
                    message
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize coupon result event", exception);
        }
    }

    // 결과 이벤트 공통 메타데이터를 채운다.
    private <T> CouponEventEnvelope<T> envelope(String eventType,
                                                String orderId,
                                                String benefitRequestId,
                                                T payload) {
        return new CouponEventEnvelope<>(
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
