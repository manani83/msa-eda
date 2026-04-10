package com.example.hexagonal.point.adapters.point.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 포인트 계정 JPA 저장소다.
 */
public interface PointAccountJpaRepository extends JpaRepository<PointAccountEntity, String> {
}
