package com.example.hexagonal.application.order;

import com.example.hexagonal.application.coupon.port.out.CouponQueryPort;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.CouponNotFoundException;
import com.example.hexagonal.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderCommandPort orderCommandPort;
    private final CouponQueryPort couponQueryPort;

    public CreateOrderService(OrderCommandPort orderCommandPort, CouponQueryPort couponQueryPort) {
        this.orderCommandPort = orderCommandPort;
        this.couponQueryPort = couponQueryPort;
    }

    @Override
    @Transactional
    public CreateOrderResult create(CreateOrderCommand command) {
        Instant now = Instant.now();
        Coupon coupon = resolveCoupon(command.getCouponCode());
        Order order = command.toOrder(coupon, now);
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
