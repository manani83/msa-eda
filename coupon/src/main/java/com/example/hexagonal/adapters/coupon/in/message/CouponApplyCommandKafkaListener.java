package com.example.hexagonal.adapters.coupon.in.message;

import com.example.hexagonal.application.coupon.CouponApplyCommandBiz;
import com.example.hexagonal.application.coupon.message.CouponApplyCommandMessage;
import com.example.hexagonal.application.coupon.message.CouponEventTopics;
import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "coupon.command.listener.enabled", havingValue = "true")
public class CouponApplyCommandKafkaListener {
    private final CouponApplyCommandBiz couponApplyCommandBiz;
    private final ObjectMapper objectMapper;

    public CouponApplyCommandKafkaListener(CouponApplyCommandBiz couponApplyCommandBiz,
                                           ObjectMapper objectMapper) {
        this.couponApplyCommandBiz = couponApplyCommandBiz;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = CouponEventTopics.COUPON_APPLY_COMMAND_V1,
            groupId = "${coupon.command.listener.group-id:coupon-command}"
    )
    // 쿠폰 적용 command를 읽어 Biz에 전달한다.
    public void listen(String message) throws IOException {
        JsonNode root = objectMapper.readTree(message);
        String eventType = text(root, "eventType");
        if (!"coupon.apply.command".equals(eventType)) {
            throw new IllegalArgumentException("Unsupported coupon event type: " + eventType);
        }

        CouponApplyCommandMessage payload = objectMapper.treeToValue(root.get("payload"), CouponApplyCommandMessage.class);
        couponApplyCommandBiz.apply(new ApplyCouponCommand(
                payload.orderId(),
                payload.benefitRequestId(),
                payload.couponCode(),
                payload.subtotalAmount(),
                payload.createdAt()
        ));
    }

    // 공통 필드를 문자열로 읽기 위한 헬퍼다.
    private String text(JsonNode root, String fieldName) {
        return root.get(fieldName).asText();
    }
}
