package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderRequest;
import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderResponse;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping("/orders")
    public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand.CreateOrderAddress address = new CreateOrderCommand.CreateOrderAddress(
                request.getShippingAddress().getZip(),
                request.getShippingAddress().getLine1(),
                request.getShippingAddress().getLine2()
        );

        List<CreateOrderCommand.CreateOrderItem> items = request.getItems().stream()
                .map(item -> new CreateOrderCommand.CreateOrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .collect(Collectors.toList());

        CreateOrderCommand command = new CreateOrderCommand(request.getUserId(), items, address);
        CreateOrderResult result = createOrderUseCase.create(command);

        return new CreateOrderResponse(
                result.getOrderId(),
                result.getStatus().name(),
                result.getTotalAmount(),
                result.getCreatedAt()
        );
    }
}
