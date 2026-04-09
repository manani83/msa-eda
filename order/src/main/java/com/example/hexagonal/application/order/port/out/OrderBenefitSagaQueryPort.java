package com.example.hexagonal.application.order.port.out;

import com.example.hexagonal.application.order.OrderBenefitSaga;

import java.util.Optional;

public interface OrderBenefitSagaQueryPort {
    Optional<OrderBenefitSaga> findByBenefitRequestId(String benefitRequestId);
}
