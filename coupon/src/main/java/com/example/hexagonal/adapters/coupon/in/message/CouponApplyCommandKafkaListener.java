package com.example.hexagonal.adapters.coupon.in.message;

import com.example.hexagonal.application.coupon.CouponApplyCommandBiz;
import com.example.hexagonal.application.coupon.message.CouponApplyCommandMessage;
import com.example.hexagonal.application.coupon.message.CouponEventEnvelope;
import com.example.hexagonal.application.coupon.message.CouponEventTopics;
import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "coupon.command.listener.enabled", havingValue = "true")
public class CouponApplyCommandKafkaListener {
    private static final Logger log = LoggerFactory.getLogger(CouponApplyCommandKafkaListener.class);
    private static final String SUPPORTED_EVENT_TYPE = "coupon.apply.command";

    private final CouponApplyCommandBiz couponApplyCommandBiz;
    private final ObjectMapper objectMapper;
    private final JavaType envelopeType;

    public CouponApplyCommandKafkaListener(CouponApplyCommandBiz couponApplyCommandBiz,
                                           ObjectMapper objectMapper) {
        this.couponApplyCommandBiz = couponApplyCommandBiz;
        this.objectMapper = objectMapper;
        this.envelopeType = objectMapper.getTypeFactory()
                .constructParametricType(CouponEventEnvelope.class, CouponApplyCommandMessage.class);
    }

    @KafkaListener(
            topics = CouponEventTopics.COUPON_APPLY_COMMAND_V1,
            groupId = "${coupon.command.listener.group-id:coupon-command}"
    )
    // 쿠폰 적용 command를 읽어 Biz에 전달한다.
    public void listen(String message) throws IOException {
        CouponEventEnvelope<CouponApplyCommandMessage> envelope = objectMapper.readValue(message, envelopeType);
        if (!SUPPORTED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new IllegalArgumentException("Unsupported coupon event type: " + envelope.eventType());
        }
        log.info(
                "Kafka receive topic={} eventType={} correlationId={} traceId={} payload={}",
                CouponEventTopics.COUPON_APPLY_COMMAND_V1,
                envelope.eventType(),
                envelope.correlationId(),
                envelope.traceId(),
                envelope.payload()
        );
        couponApplyCommandBiz.apply(new ApplyCouponCommand(
                envelope.payload().orderId(),
                envelope.payload().benefitRequestId(),
                envelope.payload().couponCode(),
                envelope.payload().subtotalAmount(),
                envelope.payload().createdAt()
        ));
    }
}
