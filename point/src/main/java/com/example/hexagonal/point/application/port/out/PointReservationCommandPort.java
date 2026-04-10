package com.example.hexagonal.point.application.port.out;

import com.example.hexagonal.point.domain.PointReservation;

/**
 * 포인트 예약 결과를 저장하는 포트다.
 */
public interface PointReservationCommandPort {
    /**
     * 포인트 예약 결과를 저장한다.
     */
    PointReservation save(PointReservation reservation);
}
