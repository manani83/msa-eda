package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateOrderBizImpl implements CreateOrderBiz {
    private final CreateOrderService createOrderService;
    private final OrderCommandPort orderCommandPort;
    private final OrderSagaOrchestrator orderSagaOrchestrator;

    public CreateOrderBizImpl(CreateOrderService createOrderService,
                              OrderCommandPort orderCommandPort,
                              OrderSagaOrchestrator orderSagaOrchestrator) {
        this.createOrderService = createOrderService;
        this.orderCommandPort = orderCommandPort;
        this.orderSagaOrchestrator = orderSagaOrchestrator;
    }

    @Override
    @Transactional
    // 주문 생성 유스케이스를 시작하고 사가 오케스트레이션으로 넘긴다.
    public CreateOrderResult create(CreateOrderCommand command) {
        Instant now = Instant.now();
        Order pendingOrder = createOrderService.createPendingOrder(command, now);
        Order saved = orderCommandPort.save(pendingOrder);
        Order orchestrated = orderSagaOrchestrator.start(saved, command.getRequestedPointAmount());

        return new CreateOrderResult(
                orchestrated.getOrderId(),
                orchestrated.getStatus(),
                orchestrated.getCouponCode(),
                orchestrated.getDiscountAmount().getAmount(),
                orchestrated.getTotalAmount().getAmount(),
                orchestrated.getCreatedAt()
        );
    }
}
