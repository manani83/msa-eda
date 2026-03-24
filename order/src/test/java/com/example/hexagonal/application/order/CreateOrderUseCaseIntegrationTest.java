package com.example.hexagonal.application.order;

import com.example.hexagonal.OrderIntegrationTestApplication;
import com.example.hexagonal.OrderMySqlTestcontainersConfig;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import com.example.hexagonal.domain.coupon.CouponValidationException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = OrderIntegrationTestApplication.class)
@Import(OrderMySqlTestcontainersConfig.class)
@Transactional
class CreateOrderUseCaseIntegrationTest {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderCommandPort orderCommandPort;

    @Test
    void create_order_persists_then_rolls_back() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                null
        );

        CreateOrderResult result = createOrderUseCase.create(command);
        System.out.println("Created orderId=" + result.getOrderId());

        boolean existsBeforeRollback = orderJpaRepository.findById(result.getOrderId()).isPresent();
        System.out.println("Exists before rollback=" + existsBeforeRollback);
        assertThat(existsBeforeRollback).isTrue();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.BENEFITS_COMPLETED);

        TestTransaction.flagForRollback();
        TestTransaction.end();

        TestTransaction.start();
        boolean existsAfterRollback = orderJpaRepository.findById(result.getOrderId()).isPresent();
        System.out.println("Exists after rollback=" + existsAfterRollback);
        assertThat(existsAfterRollback).isFalse();
    }

    @Test
    void create_order_applies_coupon_discount() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "WELCOME10"
        );

        CreateOrderResult result = createOrderUseCase.create(command);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.BENEFITS_COMPLETED);
        assertThat(result.getCouponCode()).isEqualTo("WELCOME10");
        assertThat(result.getDiscountAmount()).isEqualTo(200);
        assertThat(result.getTotalAmount()).isEqualTo(1800);
    }

    @Test
    void create_order_rejects_expired_coupon() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 1, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "EXPIRED10"
        );

        assertThatThrownBy(() -> createOrderUseCase.create(command))
                .isInstanceOf(CouponValidationException.class)
                .hasMessageContaining("Coupon is not valid");
    }

    @Test
    void create_order_rejects_coupon_below_minimum() {
        CreateOrderCommand command = new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 1, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                "SAVE1000"
        );

        assertThatThrownBy(() -> createOrderUseCase.create(command))
                .isInstanceOf(CouponValidationException.class)
                .hasMessageContaining("Order amount below minimum");
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
