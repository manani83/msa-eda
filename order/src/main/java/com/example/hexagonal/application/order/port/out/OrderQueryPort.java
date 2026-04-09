package com.example.hexagonal.application.order.port.out;

import com.example.hexagonal.domain.order.Order;

import java.util.Optional;

public interface OrderQueryPort {
    Optional<Order> findById(String orderId);
}
