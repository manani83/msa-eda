package com.example.hexagonal.adapters.order.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderBenefitSagaJpaRepository extends JpaRepository<OrderBenefitSagaEntity, Long> {
    Optional<OrderBenefitSagaEntity> findByBenefitRequestId(String benefitRequestId);

    Optional<OrderBenefitSagaEntity> findByOrderId(String orderId);
}
