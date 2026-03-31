package com.example.hexagonal.adapters.order.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutboxEntity, Long> {
    Optional<OrderOutboxEntity> findFirstByAggregateId(String aggregateId);
}
