package com.example.hexagonal.application.event.order;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String benefitRequestId,
        String userId,
        String orderStatusCode,
        String couponCode,
        Long requestedPointAmount,
        long subtotalAmount,
        Instant createdAt,
        List<OrderCreatedItem> items
) {
    public record OrderCreatedItem(
            String productId,
            int quantity,
            long unitPrice
    ) {
    }
}
