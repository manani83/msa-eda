package com.example.hexagonal.domain.coupon;

import java.time.Instant;
public class Coupon {
    private final String code;
    private final DiscountType discountType;
    private final long discountValue;
    private final long minOrderAmount;
    private final Instant validFrom;
    private final Instant validUntil;

    public Coupon(String code,
                  DiscountType discountType,
                  long discountValue,
                  long minOrderAmount,
                  Instant validFrom,
                  Instant validUntil) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (discountType == null) {
            throw new IllegalArgumentException("discountType must not be null");
        }
        if (discountValue <= 0) {
            throw new IllegalArgumentException("discountValue must be > 0");
        }
        if (discountType == DiscountType.PERCENT && discountValue > 100) {
            throw new IllegalArgumentException("percent discountValue must be <= 100");
        }
        if (minOrderAmount < 0) {
            throw new IllegalArgumentException("minOrderAmount must be >= 0");
        }
        if (validFrom == null || validUntil == null) {
            throw new IllegalArgumentException("validFrom and validUntil must not be null");
        }
        if (validFrom.isAfter(validUntil)) {
            throw new IllegalArgumentException("validFrom must be <= validUntil");
        }
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public long calculateDiscount(long baseAmount, Instant now) {
        validate(baseAmount, now);
        return Math.min(rawDiscount(baseAmount), baseAmount);
    }

    public String getCode() {
        return code;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public long getDiscountValue() {
        return discountValue;
    }

    public long getMinOrderAmount() {
        return minOrderAmount;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    private void validate(long baseAmount, Instant now) {
        if (baseAmount < 0) {
            throw new IllegalArgumentException("baseAmount must be >= 0");
        }
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (baseAmount < minOrderAmount) {
            throw new CouponValidationException("Order amount below minimum for coupon: " + code);
        }
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            throw new CouponValidationException("Coupon is not valid at this time: " + code);
        }
    }

    private long rawDiscount(long baseAmount) {
        if (discountType == DiscountType.PERCENT) {
            return baseAmount * discountValue / 100;
        }
        return discountValue;
    }
}
