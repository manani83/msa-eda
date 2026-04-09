package com.example.hexagonal.adapters.order.out.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "order.outbox.publisher.enabled", havingValue = "true")
public class OrderOutboxPublisherScheduler {
    private final OrderOutboxPublisher orderOutboxPublisher;
    private final int batchSize;

    public OrderOutboxPublisherScheduler(OrderOutboxPublisher orderOutboxPublisher,
                                         @Value("${order.outbox.publisher.batch-size:20}") int batchSize) {
        this.orderOutboxPublisher = orderOutboxPublisher;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${order.outbox.publisher.fixed-delay:5000}")
    // 설정된 주기마다 outbox 발행 작업을 실행한다.
    public void publishPending() {
        orderOutboxPublisher.publishPending(batchSize);
    }
}
