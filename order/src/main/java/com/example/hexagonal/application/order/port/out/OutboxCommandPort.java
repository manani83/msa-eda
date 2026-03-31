package com.example.hexagonal.application.order.port.out;

public interface OutboxCommandPort {
    void save(OutboxMessage outboxMessage);
}
