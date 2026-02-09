package com.example.hexagonal.adapters.coupon.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "coupons")
public class CouponEntity {
    @Id
    @Column(name = "coupon_code", length = 50)
    private String code;

    @Column(nullable = false)
    private String discountType;

    @Column(nullable = false)
    private long discountValue;

    @Column(nullable = false)
    private long minOrderAmount;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validUntil;

    protected CouponEntity() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public long getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(long discountValue) {
        this.discountValue = discountValue;
    }

    public long getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(long minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }
}
