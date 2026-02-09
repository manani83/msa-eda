package com.example.hexagonal.application.coupon.port.out;

import com.example.hexagonal.domain.coupon.Coupon;

import java.util.Optional;

public interface CouponQueryPort {
    Optional<Coupon> findByCode(String code);
}
