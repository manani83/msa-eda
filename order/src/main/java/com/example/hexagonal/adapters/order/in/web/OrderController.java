package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderRequest;
import com.example.hexagonal.adapters.order.in.web.dto.CreateOrderResponse;
import com.example.hexagonal.application.order.CreateOrderBiz;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final CreateOrderBiz createOrderBiz;

    public OrderController(CreateOrderBiz createOrderBiz) {
        this.createOrderBiz = createOrderBiz;
    }

    @PostMapping("/orders")
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        log.info(
                "HTTP request POST /orders userId={} couponCode={} pointAmount={} itemCount={} shippingZip={}",
                request.getUserId(),
                request.getCouponCode(),
                request.getPointAmount(),
                request.getItems().size(),
                request.getShippingAddress().getZip()
        );
        CreateOrderCommand command = request.toCommand();
        CreateOrderResult result = createOrderBiz.create(command);
        log.info(
                "HTTP response POST /orders orderId={} status={} couponCode={} discountAmount={} totalAmount={}",
                result.getOrderId(),
                result.getStatus().getCode(),
                result.getCouponCode(),
                result.getDiscountAmount(),
                result.getTotalAmount()
        );

        return new CreateOrderResponse(
                result.getOrderId(),
                result.getStatus().getCode(),
                result.getCouponCode(),
                result.getDiscountAmount(),
                result.getTotalAmount(),
                result.getCreatedAt()
        );
    }
}
