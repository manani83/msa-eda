package com.example.hexagonal.application.order.port.out;

import com.example.hexagonal.domain.order.Order;

public interface OrderCommandPort {
    Order save(Order order);
}
