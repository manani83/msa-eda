package com.example.hexagonal.application.event.point;

public record PointReservedEvent(
        String orderId,
        String benefitRequestId,
        String userId,
        long requestedPointAmount,
        long reservedPointAmount,
        PointResultCode resultCode
) {
}
