package com.example.hexagonal.adapters.order.out.message;

import com.example.hexagonal.OrderIntegrationTestApplication;
import com.example.hexagonal.OrderMySqlTestcontainersConfig;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxJpaRepository;
import com.example.hexagonal.application.order.CreateOrderBiz;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.out.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest(
        classes = OrderIntegrationTestApplication.class,
        properties = "order.outbox.publisher.enabled=false"
)
@Import(OrderMySqlTestcontainersConfig.class)
class OrderOutboxPublisherTest {

    @Autowired
    private CreateOrderBiz createOrderBiz;

    @Autowired
    private OrderOutboxPublisher orderOutboxPublisher;

    @Autowired
    private OrderOutboxJpaRepository orderOutboxJpaRepository;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        orderOutboxJpaRepository.deleteAll();
    }

    @Test
    void publish_pending_whenKafkaSendSucceeds_thenMarksOutboxAsPublished() {
        String orderId = createPendingOrder("WELCOME10");
        OrderOutboxEntity pending = orderOutboxJpaRepository.findFirstByAggregateId(orderId).orElseThrow();
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(null));

        int publishedCount = orderOutboxPublisher.publishPending(10);

        OrderOutboxEntity published = orderOutboxJpaRepository.findById(pending.getId()).orElseThrow();
        assertThat(publishedCount).isEqualTo(1);
        assertThat(published.getStatus()).isEqualTo(OutboxStatus.PUBLISHED.getCode());
        assertThat(published.getPublishedAt()).isNotNull();
        assertThat(published.getRetryCount()).isEqualTo(0);
        assertThat(published.getLastErrorMessage()).isNull();
    }

    @Test
    void publish_pending_whenKafkaSendFails_thenMarksOutboxAsFailed() {
        String orderId = createPendingOrder("NOPE");
        OrderOutboxEntity pending = orderOutboxJpaRepository.findFirstByAggregateId(orderId).orElseThrow();
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));

        int publishedCount = orderOutboxPublisher.publishPending(10);

        OrderOutboxEntity failed = orderOutboxJpaRepository.findById(pending.getId()).orElseThrow();
        assertThat(publishedCount).isEqualTo(1);
        assertThat(failed.getStatus()).isEqualTo(OutboxStatus.FAILED.getCode());
        assertThat(failed.getRetryCount()).isEqualTo(1);
        assertThat(failed.getLastErrorMessage()).contains("Kafka unavailable");
        assertThat(failed.getNextAttemptAt()).isAfterOrEqualTo(Instant.now().minusSeconds(1));
    }

    private String createPendingOrder(String couponCode) {
        return createOrderBiz.create(new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 1, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                couponCode,
                null
        )).getOrderId();
    }
}
