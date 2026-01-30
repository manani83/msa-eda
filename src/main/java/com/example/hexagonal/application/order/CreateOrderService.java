package com.example.hexagonal.application.order;

import com.example.hexagonal.application.coupon.port.out.CouponQueryPort;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.CouponNotFoundException;
import com.example.hexagonal.domain.order.Address;
import com.example.hexagonal.domain.order.Money;
import com.example.hexagonal.domain.order.Order;
import com.example.hexagonal.domain.order.OrderItem;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderCommandPort orderCommandPort;
    private final CouponQueryPort couponQueryPort;

    public CreateOrderService(OrderCommandPort orderCommandPort, CouponQueryPort couponQueryPort) {
        this.orderCommandPort = orderCommandPort;
        this.couponQueryPort = couponQueryPort;
    }

    @Override
    public CreateOrderResult create(CreateOrderCommand command) {
        Instant now = Instant.now();
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

        Coupon coupon = resolveCoupon(command.getCouponCode());
        Order order = Order.create(command.getUserId(), items, address, coupon, now);
        Order saved = orderCommandPort.save(order);

        return new CreateOrderResult(
                saved.getOrderId(),
                saved.getStatus(),
                saved.getCouponCode(),
                saved.getDiscountAmount().getAmount(),
                saved.getTotalAmount().getAmount(),
                saved.getCreatedAt()
        );
    }

    private Coupon resolveCoupon(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        return couponQueryPort.findByCode(couponCode)
                .orElseThrow(() -> new CouponNotFoundException(couponCode));
    }
}
