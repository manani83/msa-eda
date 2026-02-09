package com.example.hexagonal.application.order.port.in;

public interface CreateOrderUseCase {
    CreateOrderResult create(CreateOrderCommand command);
}
