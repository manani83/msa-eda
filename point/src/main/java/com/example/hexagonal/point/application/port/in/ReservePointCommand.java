package com.example.hexagonal.point.application.port.in;

import java.time.Instant;

/**
 * Kafka 포인트 예약 요청을 내부 유스케이스 입력값으로 옮긴다.
 */
public record ReservePointCommand(
        String orderId,
        String benefitRequestId,
        String userId,
        long requestedPointAmount,
        long payableAmount,
        Instant createdAt
) {
}
