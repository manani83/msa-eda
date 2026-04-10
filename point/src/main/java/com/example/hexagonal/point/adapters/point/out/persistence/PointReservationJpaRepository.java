package com.example.hexagonal.point.adapters.point.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 포인트 예약 JPA 저장소다.
 */
public interface PointReservationJpaRepository extends JpaRepository<PointReservationEntity, Long> {
    /**
     * benefitRequestId로 예약 결과를 찾는다.
     */
    Optional<PointReservationEntity> findByBenefitRequestId(String benefitRequestId);
}
