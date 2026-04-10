package com.example.hexagonal.point.application.port.out;

import com.example.hexagonal.point.domain.PointReservation;

import java.util.Optional;

/**
 * 포인트 예약 이력을 조회하는 포트다.
 */
public interface PointReservationQueryPort {
    /**
     * benefitRequestId로 예약 이력을 찾는다.
     */
    Optional<PointReservation> findByBenefitRequestId(String benefitRequestId);
}
