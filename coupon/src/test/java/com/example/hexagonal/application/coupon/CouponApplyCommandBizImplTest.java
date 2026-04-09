package com.example.hexagonal.application.coupon;

import com.example.hexagonal.application.coupon.message.CouponFailureReason;
import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;
import com.example.hexagonal.application.coupon.port.out.CouponQueryPort;
import com.example.hexagonal.application.coupon.port.out.CouponResultPublishPort;
import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponApplyCommandBizImplTest {

    @Mock
    private CouponQueryPort couponQueryPort;

    @Mock
    private CouponResultPublishPort couponResultPublishPort;

    private CouponApplyCommandBizImpl couponApplyCommandBiz;

    @BeforeEach
    void setUp() {
        couponApplyCommandBiz = new CouponApplyCommandBizImpl(couponQueryPort, couponResultPublishPort);
    }

    @Test
    void apply_whenCouponIsValid_thenPublishesAppliedResult() {
        Instant now = Instant.now();
        ApplyCouponCommand command = new ApplyCouponCommand("order-1", "benefit-order-1", "WELCOME10", 10_000L, now);
        given(couponQueryPort.findByCode("WELCOME10")).willReturn(Optional.of(validCoupon(now)));

        couponApplyCommandBiz.apply(command);

        verify(couponResultPublishPort).publishApplied("order-1", "benefit-order-1", "WELCOME10", 1_000L);
    }

    @Test
    void apply_whenCouponIsMissing_thenPublishesNotFoundResult() {
        Instant now = Instant.now();
        ApplyCouponCommand command = new ApplyCouponCommand("order-1", "benefit-order-1", "NOPE", 10_000L, now);
        given(couponQueryPort.findByCode("NOPE")).willReturn(Optional.empty());

        couponApplyCommandBiz.apply(command);

        verify(couponResultPublishPort).publishRejected(
                "order-1",
                "benefit-order-1",
                "NOPE",
                CouponFailureReason.NOT_FOUND,
                "Coupon not found: NOPE"
        );
    }

    @Test
    void apply_whenOrderAmountIsTooLow_thenPublishesBelowMinimumResult() {
        Instant now = Instant.now();
        ApplyCouponCommand command = new ApplyCouponCommand("order-1", "benefit-order-1", "WELCOME10", 4_000L, now);
        given(couponQueryPort.findByCode("WELCOME10")).willReturn(Optional.of(validCoupon(now)));

        couponApplyCommandBiz.apply(command);

        verify(couponResultPublishPort).publishRejected(
                "order-1",
                "benefit-order-1",
                "WELCOME10",
                CouponFailureReason.BELOW_MIN_ORDER_AMOUNT,
                "Order amount below minimum for coupon: WELCOME10"
        );
    }

    @Test
    void apply_whenCouponIsExpired_thenPublishesExpiredResult() {
        Instant now = Instant.now();
        ApplyCouponCommand command = new ApplyCouponCommand("order-1", "benefit-order-1", "WELCOME10", 10_000L, now);
        given(couponQueryPort.findByCode("WELCOME10")).willReturn(Optional.of(expiredCoupon(now)));

        couponApplyCommandBiz.apply(command);

        verify(couponResultPublishPort).publishRejected(
                "order-1",
                "benefit-order-1",
                "WELCOME10",
                CouponFailureReason.EXPIRED,
                "Coupon is not valid at this time: WELCOME10"
        );
    }

    private Coupon validCoupon(Instant now) {
        return new Coupon(
                "WELCOME10",
                DiscountType.PERCENT,
                10L,
                5_000L,
                now.minus(1, ChronoUnit.DAYS),
                now.plus(1, ChronoUnit.DAYS)
        );
    }

    private Coupon expiredCoupon(Instant now) {
        return new Coupon(
                "WELCOME10",
                DiscountType.PERCENT,
                10L,
                5_000L,
                now.minus(3, ChronoUnit.DAYS),
                now.minus(1, ChronoUnit.DAYS)
        );
    }
}
