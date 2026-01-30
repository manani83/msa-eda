package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import com.example.hexagonal.domain.coupon.CouponValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CreateOrderUseCaseIntegrationTest {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

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
}
