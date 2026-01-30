package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2")
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
}
