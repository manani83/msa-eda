package com.example.hexagonal.application.event.point;

import java.time.Instant;

public record PointReserveCommandEvent(
        String orderId,
        String benefitRequestId,
        String userId,
        long requestedPointAmount,
        long payableAmount,
        Instant createdAt
) {
}
