package com.example.hexagonal.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GreetingRecordJpaRepository extends JpaRepository<GreetingRecordEntity, Long> {
}
