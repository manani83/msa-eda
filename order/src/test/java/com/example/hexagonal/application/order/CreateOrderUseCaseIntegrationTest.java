package com.example.hexagonal.application.order;

import com.example.hexagonal.OrderIntegrationTestApplication;
import com.example.hexagonal.OrderMySqlTestcontainersConfig;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaJpaRepository;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxStatus;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxJpaRepository;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderIntegrationTestApplication.class)
@Import(OrderMySqlTestcontainersConfig.class)
@Transactional
class CreateOrderUseCaseIntegrationTest {

    @Autowired
    private CreateOrderBiz createOrderBiz;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderCommandPort orderCommandPort;

    @Autowired
    private OrderOutboxJpaRepository orderOutboxJpaRepository;

    @Autowired
    private OrderBenefitSagaJpaRepository orderBenefitSagaJpaRepository;

    @Test
    void create_order_with_coupon_request_persists_order_saga_and_outbox_then_rolls_back() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "WELCOME10",
                null
        );

        CreateOrderResult result = createOrderBiz.create(command);
        System.out.println("Created orderId=" + result.getOrderId());

        boolean existsBeforeRollback = orderJpaRepository.findById(result.getOrderId()).isPresent();
        OrderOutboxEntity outboxBeforeRollback = orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())
                .orElseThrow();
        OrderBenefitSagaEntity sagaBeforeRollback = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId())
                .orElseThrow();
        System.out.println("Exists before rollback=" + existsBeforeRollback);
        assertThat(existsBeforeRollback).isTrue();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(outboxBeforeRollback.getTopic()).isEqualTo(OrderEventTopics.COUPON_APPLY_COMMAND_V1);
        assertThat(outboxBeforeRollback.getStatus()).isEqualTo(OutboxStatus.PENDING.getCode());
        assertThat(sagaBeforeRollback.getCurrentStep()).isEqualTo(OrderSagaStep.WAITING_COUPON_RESULT.getCode());

        TestTransaction.flagForRollback();
        TestTransaction.end();

        TestTransaction.start();
        boolean existsAfterRollback = orderJpaRepository.findById(result.getOrderId()).isPresent();
        System.out.println("Exists after rollback=" + existsAfterRollback);
        assertThat(existsAfterRollback).isFalse();
        assertThat(orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())).isEmpty();
        assertThat(orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId())).isEmpty();
    }

    @Test
    void create_order_with_coupon_request_returns_pending_benefits_and_stores_coupon_apply_command() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "WELCOME10",
                null
        );

        CreateOrderResult result = createOrderBiz.create(command);
        OrderOutboxEntity outbox = orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())
                .orElseThrow();

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(result.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(2000);
        assertThat(outbox.getEventType()).isEqualTo("coupon.apply.command");
        assertThat(outbox.getPayloadJson()).contains("\"couponCode\":\"WELCOME10\"");
        assertThat(outbox.getPayloadJson()).contains("\"subtotalAmount\":2000");
    }

    @Test
    void create_order_with_point_request_stores_point_reserve_command() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                null,
                300L
        );

        CreateOrderResult result = createOrderBiz.create(command);
        OrderOutboxEntity outbox = orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())
                .orElseThrow();
        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId())
                .orElseThrow();

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(result.getCouponCode()).isNull();
        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(2000);
        assertThat(outbox.getTopic()).isEqualTo(OrderEventTopics.POINT_RESERVE_COMMAND_V1);
        assertThat(outbox.getEventType()).isEqualTo("point.reserve.command");
        assertThat(outbox.getPayloadJson()).contains("\"requestedPointAmount\":300");
        assertThat(saga.getCurrentStep()).isEqualTo(OrderSagaStep.WAITING_POINT_RESULT.getCode());
    }

    @Test
    void create_order_without_benefit_request_completes_immediately() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                null,
                null
        );

        CreateOrderResult result = createOrderBiz.create(command);
        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId())
                .orElseThrow();

        assertThat(result.getStatus()).isEqualTo(OrderStatus.BENEFITS_COMPLETED);
        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(2000);
        assertThat(orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())).isEmpty();
        assertThat(saga.getCurrentStep()).isEqualTo(OrderSagaStep.COMPLETED.getCode());
    }

    @Test
    void create_order_accepts_coupon_code_without_synchronous_validation() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 1, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "NOPE",
                null
        );

        CreateOrderResult result = createOrderBiz.create(command);
        OrderOutboxEntity outbox = orderOutboxJpaRepository.findFirstByAggregateId(result.getOrderId())
                .orElseThrow();

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_BENEFITS);
        assertThat(result.getCouponCode()).isEqualTo("NOPE");
        assertThat(result.getDiscountAmount()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(1000);
        assertThat(outbox.getTopic()).isEqualTo(OrderEventTopics.COUPON_APPLY_COMMAND_V1);
        assertThat(outbox.getPayloadJson()).contains("\"couponCode\":\"NOPE\"");
    }

    @Test
    void save_order_preserves_created_at_from_domain() {
        Instant createdAt = Instant.parse("2026-03-20T00:00:00Z");
        Order order = Order.create(
                "user-1",
                List.of(new OrderItem("prod-1", 1, Money.of(1000))),
                new Address("12345", "line1", "line2"),
                null,
                createdAt
        );

        Order saved = orderCommandPort.save(order);

        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getOrderId()).startsWith("20260320");
    }
}
