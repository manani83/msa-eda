package com.example.hexagonal.application.order;

import com.example.hexagonal.domain.order.Order;

import java.time.Instant;

public class OrderBenefitSaga {
    private final Long id;
    private final Long version;
    private final String benefitRequestId;
    private final String orderId;
    private final String userId;
    private final String couponCode;
    private final long requestedPointAmount;
    private final OrderSagaStep currentStep;
    private final CouponProcessStatus couponStatus;
    private final PointProcessStatus pointStatus;
    private final long couponDiscountAmount;
    private final long reservedPointAmount;
    private final String lastFailureCode;
    private final Instant createdAt;
    private final Instant updatedAt;

    private OrderBenefitSaga(Long id,
                             Long version,
                             String benefitRequestId,
                             String orderId,
                             String userId,
                             String couponCode,
                             long requestedPointAmount,
                             OrderSagaStep currentStep,
                             CouponProcessStatus couponStatus,
                             PointProcessStatus pointStatus,
                             long couponDiscountAmount,
                             long reservedPointAmount,
                             String lastFailureCode,
                             Instant createdAt,
                             Instant updatedAt) {
        this.id = id;
        this.version = version;
        this.benefitRequestId = benefitRequestId;
        this.orderId = orderId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.requestedPointAmount = requestedPointAmount;
        this.currentStep = currentStep;
        this.couponStatus = couponStatus;
        this.pointStatus = pointStatus;
        this.couponDiscountAmount = couponDiscountAmount;
        this.reservedPointAmount = reservedPointAmount;
        this.lastFailureCode = lastFailureCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 주문 요청 기준으로 첫 사가 상태를 만든다.
    public static OrderBenefitSaga start(String benefitRequestId, Order order, long requestedPointAmount) {
        Instant now = order.getCreatedAt();
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            return new OrderBenefitSaga(
                    null,
                    null,
                    benefitRequestId,
                    order.getOrderId(),
                    order.getUserId(),
                    order.getCouponCode(),
                    requestedPointAmount,
                    OrderSagaStep.WAITING_COUPON_RESULT,
                    CouponProcessStatus.REQUESTED,
                    requestedPointAmount > 0 ? PointProcessStatus.REQUESTED : PointProcessStatus.NOT_REQUESTED,
                    0,
                    0,
                    null,
                    now,
                    now
            );
        }
        if (requestedPointAmount > 0) {
            return new OrderBenefitSaga(
                    null,
                    null,
                    benefitRequestId,
                    order.getOrderId(),
                    order.getUserId(),
                    null,
                    requestedPointAmount,
                    OrderSagaStep.WAITING_POINT_RESULT,
                    CouponProcessStatus.NOT_REQUESTED,
                    PointProcessStatus.REQUESTED,
                    0,
                    0,
                    null,
                    now,
                    now
            );
        }
        return new OrderBenefitSaga(
                null,
                null,
                benefitRequestId,
                order.getOrderId(),
                order.getUserId(),
                null,
                0,
                OrderSagaStep.COMPLETED,
                CouponProcessStatus.NOT_REQUESTED,
                PointProcessStatus.NOT_REQUESTED,
                0,
                0,
                null,
                now,
                now
        );
    }

    // 저장된 사가 데이터를 도메인 객체로 복원한다.
    public static OrderBenefitSaga rehydrate(Long id,
                                             Long version,
                                             String benefitRequestId,
                                             String orderId,
                                             String userId,
                                             String couponCode,
                                             long requestedPointAmount,
                                             OrderSagaStep currentStep,
                                             CouponProcessStatus couponStatus,
                                             PointProcessStatus pointStatus,
                                             long couponDiscountAmount,
                                             long reservedPointAmount,
                                             String lastFailureCode,
                                             Instant createdAt,
                                             Instant updatedAt) {
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                currentStep,
                couponStatus,
                pointStatus,
                couponDiscountAmount,
                reservedPointAmount,
                lastFailureCode,
                createdAt,
                updatedAt
        );
    }

    // 쿠폰 처리 단계가 필요한 요청인지 판단한다.
    public boolean hasCouponRequest() {
        return couponStatus != CouponProcessStatus.NOT_REQUESTED;
    }

    // 포인트 처리 단계가 필요한 요청인지 판단한다.
    public boolean hasPointRequest() {
        return requestedPointAmount > 0;
    }

    // 추가 혜택 처리 없이 바로 완료 상태로 전환한다.
    public OrderBenefitSaga completeWithoutBenefits() {
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                OrderSagaStep.COMPLETED,
                couponStatus,
                pointStatus,
                couponDiscountAmount,
                reservedPointAmount,
                null,
                createdAt,
                Instant.now()
        );
    }

    // 쿠폰 성공 결과를 반영하고 다음 단계를 결정한다.
    public OrderBenefitSaga couponApplied(long discountAmount) {
        OrderSagaStep nextStep = hasPointRequest() ? OrderSagaStep.WAITING_POINT_RESULT : OrderSagaStep.COMPLETED;
        PointProcessStatus nextPointStatus = hasPointRequest() ? PointProcessStatus.REQUESTED : pointStatus;
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                nextStep,
                CouponProcessStatus.APPLIED,
                nextPointStatus,
                discountAmount,
                reservedPointAmount,
                null,
                createdAt,
                Instant.now()
        );
    }

    // 쿠폰 실패 결과를 반영하고 사가를 실패 상태로 전환한다.
    public OrderBenefitSaga couponRejected(String failureCode) {
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                OrderSagaStep.FAILED,
                CouponProcessStatus.REJECTED,
                pointStatus,
                couponDiscountAmount,
                reservedPointAmount,
                failureCode,
                createdAt,
                Instant.now()
        );
    }

    // 포인트 성공 결과를 반영하고 사가를 완료 상태로 전환한다.
    public OrderBenefitSaga pointReserved(long reservedPointAmount) {
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                OrderSagaStep.COMPLETED,
                couponStatus,
                PointProcessStatus.RESERVED,
                couponDiscountAmount,
                reservedPointAmount,
                null,
                createdAt,
                Instant.now()
        );
    }

    // 포인트 실패 결과를 반영하고 사가를 실패 상태로 전환한다.
    public OrderBenefitSaga pointFailed(String failureCode) {
        return new OrderBenefitSaga(
                id,
                version,
                benefitRequestId,
                orderId,
                userId,
                couponCode,
                requestedPointAmount,
                OrderSagaStep.FAILED,
                couponStatus,
                PointProcessStatus.FAILED,
                couponDiscountAmount,
                reservedPointAmount,
                failureCode,
                createdAt,
                Instant.now()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getBenefitRequestId() {
        return benefitRequestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public long getRequestedPointAmount() {
        return requestedPointAmount;
    }

    public OrderSagaStep getCurrentStep() {
        return currentStep;
    }

    public CouponProcessStatus getCouponStatus() {
        return couponStatus;
    }

    public PointProcessStatus getPointStatus() {
        return pointStatus;
    }

    public long getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public long getReservedPointAmount() {
        return reservedPointAmount;
    }

    public String getLastFailureCode() {
        return lastFailureCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
