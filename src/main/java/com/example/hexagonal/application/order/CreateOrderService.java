package com.example.hexagonal.application.order;

import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderCommandPort orderCommandPort;

    public CreateOrderService(OrderCommandPort orderCommandPort) {
        this.orderCommandPort = orderCommandPort;
    }

    @Override
    public CreateOrderResult create(CreateOrderCommand command) {
        Address address = new Address(
                command.getShippingAddress().getZip(),
                command.getShippingAddress().getLine1(),
                command.getShippingAddress().getLine2()
        );

        List<OrderItem> items = command.getItems().stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        Money.of(item.getUnitPrice())
                ))
                .collect(Collectors.toList());

        Order order = Order.create(command.getUserId(), items, address);
        Order saved = orderCommandPort.save(order);

        return new CreateOrderResult(
                saved.getOrderId(),
                saved.getStatus(),
                saved.getTotalAmount().getAmount(),
                saved.getCreatedAt()
        );
    }
}
