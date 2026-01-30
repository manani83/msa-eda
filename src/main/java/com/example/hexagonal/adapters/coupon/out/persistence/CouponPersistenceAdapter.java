package com.example.hexagonal.adapters.coupon.out.persistence;

import com.example.hexagonal.application.coupon.port.out.CouponQueryPort;
import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.DiscountType;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CouponPersistenceAdapter implements CouponQueryPort {
    private final CouponJpaRepository couponJpaRepository;

    public CouponPersistenceAdapter(CouponJpaRepository couponJpaRepository) {
        this.couponJpaRepository = couponJpaRepository;
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return couponJpaRepository.findById(code).map(this::toDomain);
    }

    private Coupon toDomain(CouponEntity entity) {
        return new Coupon(
                entity.getCode(),
                DiscountType.valueOf(entity.getDiscountType()),
                entity.getDiscountValue(),
                entity.getMinOrderAmount(),
                entity.getValidFrom(),
                entity.getValidUntil()
        );
    }
}
