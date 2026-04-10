package com.example.hexagonal.point.application.port.out;

import com.example.hexagonal.application.event.point.PointFailureReason;

/**
 * 포인트 예약 결과를 Kafka로 발행하는 포트다.
 */
public interface PointResultPublishPort {
    /**
     * 포인트 예약 성공 결과를 발행한다.
     */
    void publishReserved(String orderId,
                         String benefitRequestId,
                         String userId,
                         long requestedPointAmount,
                         long reservedPointAmount);

    /**
     * 포인트 예약 실패 결과를 발행한다.
     */
    void publishFailed(String orderId,
                       String benefitRequestId,
                       String userId,
                       long requestedPointAmount,
                       PointFailureReason reason,
                       String reasonMessage);
}
