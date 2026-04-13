package com.example.hexagonal;

import com.example.hexagonal.application.event.order.OrderEventTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka E2E 테스트에서 필요한 토픽을 미리 만든다.
 */
@TestConfiguration
public class AppKafkaTopicsConfig {

    @Bean
    public NewTopic couponApplyCommandTopic() {
        return TopicBuilder.name(OrderEventTopics.COUPON_APPLY_COMMAND_V1)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic couponApplyResultTopic() {
        return TopicBuilder.name(OrderEventTopics.COUPON_APPLY_RESULT_V1)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pointReserveCommandTopic() {
        return TopicBuilder.name(OrderEventTopics.POINT_RESERVE_COMMAND_V1)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pointReserveResultTopic() {
        return TopicBuilder.name(OrderEventTopics.POINT_RESERVE_RESULT_V1)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
