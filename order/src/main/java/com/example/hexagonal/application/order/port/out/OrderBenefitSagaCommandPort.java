package com.example.hexagonal.application.order.port.out;

import com.example.hexagonal.application.order.OrderBenefitSaga;

public interface OrderBenefitSagaCommandPort {
    OrderBenefitSaga save(OrderBenefitSaga saga);
}
