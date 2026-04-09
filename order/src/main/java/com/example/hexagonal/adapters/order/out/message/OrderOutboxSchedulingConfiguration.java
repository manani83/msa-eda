package com.example.hexagonal.adapters.order.out.message;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "order.outbox.publisher.enabled", havingValue = "true")
public class OrderOutboxSchedulingConfiguration {
}
