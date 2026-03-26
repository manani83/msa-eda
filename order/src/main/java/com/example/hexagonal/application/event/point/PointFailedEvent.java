package com.example.hexagonal.application.event.point;

public record PointFailedEvent(
        String orderId,
        String benefitRequestId,
        String userId,
        long requestedPointAmount,
        PointResultCode resultCode,
        PointFailureReason reasonCode,
        String reasonMessage
) {
}
