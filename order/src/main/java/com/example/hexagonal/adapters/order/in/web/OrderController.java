package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderRequest;
import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderResponse;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping("/orders")
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = request.toCommand();
        CreateOrderResult result = createOrderUseCase.create(command);

        return new CreateOrderResponse(
                result.getOrderId(),
                result.getStatus().name(),
                result.getCouponCode(),
                result.getDiscountAmount(),
                result.getTotalAmount(),
                result.getCreatedAt()
        );
    }
}
