package com.example.hexagonal.point.application.port.in;

/**
 * 포인트 예약 유스케이스의 진입점이다.
 */
public interface PointReserveCommandBiz {
    /**
     * 주문에서 전달된 포인트 예약 요청을 처리한다.
     */
    void reserve(ReservePointCommand command);
}
