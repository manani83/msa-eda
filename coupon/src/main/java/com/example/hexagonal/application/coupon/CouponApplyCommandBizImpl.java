package com.example.hexagonal.application.coupon;

import com.example.hexagonal.application.coupon.message.CouponFailureReason;
import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;
import com.example.hexagonal.application.coupon.port.out.CouponQueryPort;
import com.example.hexagonal.application.coupon.port.out.CouponResultPublishPort;
import com.example.hexagonal.domain.coupon.Coupon;
import com.example.hexagonal.domain.coupon.CouponValidationException;
import org.springframework.stereotype.Service;

@Service
public class CouponApplyCommandBizImpl implements CouponApplyCommandBiz {
    private final CouponQueryPort couponQueryPort;
    private final CouponResultPublishPort couponResultPublishPort;

    public CouponApplyCommandBizImpl(CouponQueryPort couponQueryPort,
                                     CouponResultPublishPort couponResultPublishPort) {
        this.couponQueryPort = couponQueryPort;
        this.couponResultPublishPort = couponResultPublishPort;
    }

    @Override
    // 쿠폰 적용 command를 처리하고 결과 이벤트를 발행한다.
    public void apply(ApplyCouponCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        if (isInvalidCommand(command)) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.INVALID_STATE,
                    "Coupon apply command is invalid"
            );
            return;
        }

        Coupon coupon = couponQueryPort.findByCode(command.couponCode()).orElse(null);
        if (coupon == null) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.NOT_FOUND,
                    "Coupon not found: " + command.couponCode()
            );
            return;
        }

        if (command.subtotalAmount() < coupon.getMinOrderAmount()) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.BELOW_MIN_ORDER_AMOUNT,
                    "Order amount below minimum for coupon: " + command.couponCode()
            );
            return;
        }

        if (command.createdAt().isBefore(coupon.getValidFrom()) || command.createdAt().isAfter(coupon.getValidUntil())) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.EXPIRED,
                    "Coupon is not valid at this time: " + command.couponCode()
            );
            return;
        }

        try {
            long discountAmount = coupon.calculateDiscount(command.subtotalAmount(), command.createdAt());
            couponResultPublishPort.publishApplied(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    discountAmount
            );
        } catch (CouponValidationException exception) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.INVALID_STATE,
                    exception.getMessage()
            );
        } catch (RuntimeException exception) {
            couponResultPublishPort.publishRejected(
                    command.orderId(),
                    command.benefitRequestId(),
                    command.couponCode(),
                    CouponFailureReason.INTERNAL_ERROR,
                    exception.getMessage()
            );
        }
    }

    // 필수 값이 비어 있으면 처리 불가 command로 본다.
    private boolean isInvalidCommand(ApplyCouponCommand command) {
        return command.orderId() == null
                || command.orderId().isBlank()
                || command.benefitRequestId() == null
                || command.benefitRequestId().isBlank()
                || command.couponCode() == null
                || command.couponCode().isBlank()
                || command.createdAt() == null
                || command.subtotalAmount() < 0;
    }
}
