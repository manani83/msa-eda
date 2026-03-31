package com.example.hexagonal.application.order;

import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.order.OrderCreatedEvent;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxMessage;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderBizImpl implements CreateOrderBiz {
    private static final String ORDER_EVENT_TYPE = "order.created";
    private static final String ORDER_EVENT_PRODUCER = "order-service";
    private static final String ORDER_AGGREGATE_TYPE = "order";
    private static final int ORDER_EVENT_SCHEMA_VERSION = 1;

    private final CreateOrderService createOrderService;
    private final OrderCommandPort orderCommandPort;
    private final OutboxCommandPort outboxCommandPort;

    public CreateOrderBizImpl(CreateOrderService createOrderService,
                              OrderCommandPort orderCommandPort,
                              OutboxCommandPort outboxCommandPort) {
        this.createOrderService = createOrderService;
        this.orderCommandPort = orderCommandPort;
        this.outboxCommandPort = outboxCommandPort;
    }

    @Override
    @Transactional
    public CreateOrderResult create(CreateOrderCommand command) {
        Instant now = Instant.now();
        Order pendingOrder = createOrderService.createPendingOrder(command, now);
        Order saved = orderCommandPort.save(pendingOrder);

        outboxCommandPort.save(OutboxMessage.pending(
                OrderEventTopics.ORDER_CREATED_V1,
                OrderEventTopics.messageKey(saved.getOrderId()),
                buildOrderCreatedEnvelope(saved)
        ));

        return new CreateOrderResult(
                saved.getOrderId(),
                saved.getStatus(),
                saved.getCouponCode(),
                saved.getDiscountAmount().getAmount(),
                saved.getTotalAmount().getAmount(),
                saved.getCreatedAt()
        );
    }

    private EventEnvelope<OrderCreatedEvent> buildOrderCreatedEnvelope(Order order) {
        String benefitRequestId = "benefit-" + order.getOrderId();
        OrderCreatedEvent payload = new OrderCreatedEvent(
                order.getOrderId(),
                benefitRequestId,
                order.getUserId(),
                order.getStatus().getCode(),
                order.getCouponCode(),
                null,
                order.getTotalAmount().getAmount(),
                order.getCreatedAt(),
                toItems(order.getItems())
        );

        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                ORDER_EVENT_TYPE,
                ORDER_EVENT_SCHEMA_VERSION,
                order.getCreatedAt(),
                ORDER_EVENT_PRODUCER,
                ORDER_AGGREGATE_TYPE,
                order.getOrderId(),
                benefitRequestId,
                order.getOrderId(),
                payload
        );
    }

    private List<OrderCreatedEvent.OrderCreatedItem> toItems(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderCreatedEvent.OrderCreatedItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice().getAmount()
                ))
                .toList();
    }
}
