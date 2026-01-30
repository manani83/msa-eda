package com.example.hexagonal.adapters.coupon.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, String> {
}
