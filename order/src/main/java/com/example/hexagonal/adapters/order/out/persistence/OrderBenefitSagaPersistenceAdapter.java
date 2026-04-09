package com.example.hexagonal.adapters.order.out.persistence;

import com.example.hexagonal.application.order.CouponProcessStatus;
import com.example.hexagonal.application.order.OrderBenefitSaga;
import com.example.hexagonal.application.order.OrderSagaStep;
import com.example.hexagonal.application.order.PointProcessStatus;
import com.example.hexagonal.application.order.port.out.OrderBenefitSagaCommandPort;
import com.example.hexagonal.application.order.port.out.OrderBenefitSagaQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderBenefitSagaPersistenceAdapter implements OrderBenefitSagaCommandPort, OrderBenefitSagaQueryPort {
    private final OrderBenefitSagaJpaRepository repository;

    public OrderBenefitSagaPersistenceAdapter(OrderBenefitSagaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderBenefitSaga save(OrderBenefitSaga saga) {
        OrderBenefitSagaEntity entity = toEntity(saga);
        OrderBenefitSagaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OrderBenefitSaga> findByBenefitRequestId(String benefitRequestId) {
        return repository.findByBenefitRequestId(benefitRequestId)
                .map(this::toDomain);
    }

    private OrderBenefitSagaEntity toEntity(OrderBenefitSaga saga) {
        OrderBenefitSagaEntity entity = new OrderBenefitSagaEntity();
        if (saga.getId() != null) {
            entity = repository.findById(saga.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + saga.getId()));
        }
        entity.setBenefitRequestId(saga.getBenefitRequestId());
        entity.setOrderId(saga.getOrderId());
        entity.setUserId(saga.getUserId());
        entity.setCouponCode(saga.getCouponCode());
        entity.setRequestedPointAmount(saga.getRequestedPointAmount());
        entity.setCurrentStep(saga.getCurrentStep().getCode());
        entity.setCouponStatus(saga.getCouponStatus().getCode());
        entity.setPointStatus(saga.getPointStatus().getCode());
        entity.setCouponDiscountAmount(saga.getCouponDiscountAmount());
        entity.setReservedPointAmount(saga.getReservedPointAmount());
        entity.setLastFailureCode(saga.getLastFailureCode());
        entity.setCreatedAt(saga.getCreatedAt());
        entity.setUpdatedAt(saga.getUpdatedAt());
        return entity;
    }

    private OrderBenefitSaga toDomain(OrderBenefitSagaEntity entity) {
        return OrderBenefitSaga.rehydrate(
                entity.getId(),
                entity.getVersion(),
                entity.getBenefitRequestId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getCouponCode(),
                entity.getRequestedPointAmount(),
                OrderSagaStep.fromCode(entity.getCurrentStep()),
                CouponProcessStatus.fromCode(entity.getCouponStatus()),
                PointProcessStatus.fromCode(entity.getPointStatus()),
                entity.getCouponDiscountAmount(),
                entity.getReservedPointAmount(),
                entity.getLastFailureCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
