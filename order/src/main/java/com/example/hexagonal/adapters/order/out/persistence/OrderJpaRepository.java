package com.example.hexagonal.adapters.order.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {
}
