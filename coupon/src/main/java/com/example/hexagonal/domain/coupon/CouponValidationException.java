package com.example.hexagonal.domain.coupon;

public class CouponValidationException extends RuntimeException {
    public CouponValidationException(String message) {
        super(message);
    }
}
