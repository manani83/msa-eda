package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.domain.order.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CreateOrderService {
    // 요청 정보를 기반으로 혜택 처리 대기 상태의 주문을 만든다.
    public Order createPendingOrder(CreateOrderCommand command, Instant createdAt) {
        return command.toPendingOrder(createdAt);
    }
}
