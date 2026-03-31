package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.domain.order.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CreateOrderService {
    public Order createPendingOrder(CreateOrderCommand command, Instant createdAt) {
        return command.toPendingOrder(createdAt);
    }
}
