package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.AppKafkaTopicsConfig;
import com.example.hexagonal.AppMySqlTestcontainersConfig;
import com.example.hexagonal.HexagonalApiApplication;
import com.example.hexagonal.application.coupon.CouponApplyCommandBiz;
import com.example.hexagonal.adapters.order.out.message.OrderOutboxPublisher;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaJpaRepository;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxJpaRepository;
import com.example.hexagonal.application.order.CouponProcessStatus;
import com.example.hexagonal.application.order.OrderSagaStep;
import com.example.hexagonal.application.order.OrderSagaOrchestrator;
import com.example.hexagonal.application.order.PointProcessStatus;
import com.example.hexagonal.application.order.port.out.OutboxStatus;
import com.example.hexagonal.application.coupon.port.out.CouponResultPublishPort;
import com.example.hexagonal.domain.order.OrderStatus;
import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 실제 Kafka 브로커를 붙여 주문 생성부터 coupon/point 완료 및 실패 흐름을 검증한다.
 */
@Testcontainers
@SpringBootTest(
        classes = HexagonalApiApplication.class,
        properties = {
                "order.outbox.publisher.enabled=false",
                "order.saga.listener.enabled=true",
                "coupon.command.listener.enabled=true",
                "point.command.listener.enabled=true",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.listener.missing-topics-fatal=false"
        }
)
@AutoConfigureMockMvc
@Import({AppMySqlTestcontainersConfig.class, AppKafkaTopicsConfig.class})
class OrderKafkaE2eTest {

    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderOutboxPublisher orderOutboxPublisher;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderBenefitSagaJpaRepository orderBenefitSagaJpaRepository;

    @Autowired
    private OrderOutboxJpaRepository orderOutboxJpaRepository;

    @SpyBean
    private CouponApplyCommandBiz couponApplyCommandBiz;

    @SpyBean
    private PointReserveCommandBiz pointReserveCommandBiz;

    @SpyBean
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @SpyBean
    private CouponResultPublishPort couponResultPublishPort;

    @Test
    void create_order_with_coupon_and_point_then_completes_via_kafka() throws Exception {
        String orderId = createOrder("WELCOME10", 300L);

        waitForOutboxRecord(orderId);
        orderOutboxPublisher.publishPending(20);
        assertThat(orderOutboxJpaRepository.findFirstByAggregateId(orderId).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED.getCode());
        verify(couponApplyCommandBiz, org.mockito.Mockito.timeout(15000).atLeastOnce()).apply(any());
        verify(couponResultPublishPort, org.mockito.Mockito.timeout(15000).atLeastOnce())
                .publishApplied(any(), any(), any(), anyLong());
        verify(orderSagaOrchestrator, org.mockito.Mockito.timeout(15000).atLeastOnce()).handleCouponApplied(any());
        waitForOrderStatus(orderId, OrderStatus.BENEFITS_COMPLETED.getCode());
        verify(pointReserveCommandBiz, org.mockito.Mockito.timeout(15000).atLeastOnce()).reserve(any());
        verify(orderSagaOrchestrator, org.mockito.Mockito.timeout(15000).atLeastOnce()).handlePointReserved(any());

        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(orderId).orElseThrow();
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.BENEFITS_COMPLETED.getCode());
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getDiscountAmount())
                .isEqualTo(500L);
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getTotalAmount())
                .isEqualTo(1500L);
        assertThat(saga.getCurrentStep()).isEqualTo(OrderSagaStep.COMPLETED.getCode());
        assertThat(saga.getCouponStatus()).isEqualTo(CouponProcessStatus.APPLIED.getCode());
        assertThat(saga.getPointStatus()).isEqualTo(PointProcessStatus.RESERVED.getCode());
    }

    @Test
    void create_order_with_coupon_and_insufficient_point_then_partially_fails_via_kafka() throws Exception {
        String orderId = createOrder("WELCOME10", 15_000L);

        waitForOutboxRecord(orderId);
        orderOutboxPublisher.publishPending(20);
        assertThat(orderOutboxJpaRepository.findFirstByAggregateId(orderId).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED.getCode());
        verify(couponApplyCommandBiz, org.mockito.Mockito.timeout(15000).atLeastOnce()).apply(any());
        verify(couponResultPublishPort, org.mockito.Mockito.timeout(15000).atLeastOnce())
                .publishApplied(any(), any(), any(), anyLong());
        verify(orderSagaOrchestrator, org.mockito.Mockito.timeout(15000).atLeastOnce()).handleCouponApplied(any());
        waitForOrderStatus(orderId, OrderStatus.PARTIAL_FAILED.getCode());
        verify(pointReserveCommandBiz, org.mockito.Mockito.timeout(15000).atLeastOnce()).reserve(any());
        verify(orderSagaOrchestrator, org.mockito.Mockito.timeout(15000).atLeastOnce()).handlePointFailed(any());

        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(orderId).orElseThrow();
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PARTIAL_FAILED.getCode());
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getDiscountAmount())
                .isEqualTo(200L);
        assertThat(orderJpaRepository.findById(orderId).orElseThrow().getTotalAmount())
                .isEqualTo(1800L);
        assertThat(saga.getCurrentStep()).isEqualTo(OrderSagaStep.FAILED.getCode());
        assertThat(saga.getCouponStatus()).isEqualTo(CouponProcessStatus.APPLIED.getCode());
        assertThat(saga.getPointStatus()).isEqualTo(PointProcessStatus.FAILED.getCode());
    }

    private String createOrder(String couponCode, Long pointAmount) throws Exception {
        String payload = """
                {
                  "userId": "user-1",
                  "items": [{"productId": "prod-1", "quantity": 2, "unitPrice": 1000}],
                  "shippingAddress": {"zip": "12345", "line1": "line1", "line2": "line2"},
                  "couponCode": %s,
                  "pointAmount": %s
                }
                """.formatted(
                couponCode == null ? "null" : "\"" + couponCode + "\"",
                pointAmount == null ? "null" : pointAmount.toString()
        );

        String responseBody = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING_BENEFITS.getCode()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(responseBody);
        assertThat(root.get("couponCode").asText()).isEqualTo(couponCode);
        return root.get("orderId").asText();
    }

    /**
     * 주문 생성 직후 outbox 레코드가 커밋될 때까지 잠깐 대기한다.
     */
    private void waitForOutboxRecord(String orderId) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (orderOutboxJpaRepository.findFirstByAggregateId(orderId).isPresent()) {
                return;
            }
            Thread.sleep(Duration.ofMillis(100).toMillis());
        }

        fail("Timed out waiting for outbox record for order " + orderId);
    }

    /**
     * outbox를 직접 발행하면서 최종 주문 상태가 기대값이 될 때까지 기다린다.
     */
    private void waitForOrderStatus(String orderId, String expectedStatus) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120);
        while (System.nanoTime() < deadline) {
            orderOutboxPublisher.publishPending(20);
            String currentStatus = orderJpaRepository.findById(orderId)
                    .orElseThrow()
                    .getStatus();
            if (expectedStatus.equals(currentStatus)) {
                return;
            }
            Thread.sleep(Duration.ofMillis(250).toMillis());
        }

        fail("Timed out waiting for order status " + expectedStatus + " for order " + orderId);
    }
}
