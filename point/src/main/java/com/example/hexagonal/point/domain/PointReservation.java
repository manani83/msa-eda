package com.example.hexagonal.point.domain;

import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.point.PointResultCode;

import java.time.Instant;

/**
 * 포인트 예약 결과와 실패 사유를 함께 담는 도메인 객체다.
 */
public class PointReservation {
    private final String benefitRequestId;
    private final String orderId;
    private final String userId;
    private final long requestedPointAmount;
    private final long reservedPointAmount;
    private final PointResultCode resultCode;
    private final PointFailureReason failureReason;
    private final String failureMessage;
    private final Instant createdAt;
    private final Instant updatedAt;

    private PointReservation(String benefitRequestId,
                             String orderId,
                             String userId,
                             long requestedPointAmount,
                             long reservedPointAmount,
                             PointResultCode resultCode,
                             PointFailureReason failureReason,
                             String failureMessage,
                             Instant createdAt,
                             Instant updatedAt) {
        if (benefitRequestId == null || benefitRequestId.isBlank()) {
            throw new IllegalArgumentException("benefitRequestId must not be blank");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (requestedPointAmount < 0) {
            throw new IllegalArgumentException("requestedPointAmount must be >= 0");
        }
        if (reservedPointAmount < 0) {
            throw new IllegalArgumentException("reservedPointAmount must be >= 0");
        }
        if (reservedPointAmount > requestedPointAmount) {
            throw new IllegalArgumentException("reservedPointAmount must be <= requestedPointAmount");
        }
        if (resultCode == null) {
            throw new IllegalArgumentException("resultCode must not be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt and updatedAt must not be null");
        }
        if (resultCode == PointResultCode.FAILED && failureReason == null) {
            throw new IllegalArgumentException("failureReason must not be null for failed reservation");
        }
        this.benefitRequestId = benefitRequestId;
        this.orderId = orderId;
        this.userId = userId;
        this.requestedPointAmount = requestedPointAmount;
        this.reservedPointAmount = reservedPointAmount;
        this.resultCode = resultCode;
        this.failureReason = failureReason;
        this.failureMessage = failureMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 예약 성공 상태의 예약을 만든다.
     */
    public static PointReservation reserved(String benefitRequestId,
                                            String orderId,
                                            String userId,
                                            long requestedPointAmount,
                                            long reservedPointAmount,
                                            Instant now) {
        return new PointReservation(
                benefitRequestId,
                orderId,
                userId,
                requestedPointAmount,
                reservedPointAmount,
                PointResultCode.RESERVED,
                null,
                null,
                now,
                now
        );
    }

    /**
     * 예약 실패 상태의 예약을 만든다.
     */
    public static PointReservation failed(String benefitRequestId,
                                          String orderId,
                                          String userId,
                                          long requestedPointAmount,
                                          PointFailureReason failureReason,
                                          String failureMessage,
                                          Instant now) {
        return new PointReservation(
                benefitRequestId,
                orderId,
                userId,
                requestedPointAmount,
                0,
                PointResultCode.FAILED,
                failureReason,
                failureMessage,
                now,
                now
        );
    }

    /**
     * 저장소에서 읽은 값을 도메인 객체로 복원한다.
     */
    public static PointReservation rehydrate(String benefitRequestId,
                                             String orderId,
                                             String userId,
                                             long requestedPointAmount,
                                             long reservedPointAmount,
                                             PointResultCode resultCode,
                                             PointFailureReason failureReason,
                                             String failureMessage,
                                             Instant createdAt,
                                             Instant updatedAt) {
        return new PointReservation(
                benefitRequestId,
                orderId,
                userId,
                requestedPointAmount,
                reservedPointAmount,
                resultCode,
                failureReason,
                failureMessage,
                createdAt,
                updatedAt
        );
    }

    /**
     * 예약 성공 여부를 확인한다.
     */
    public boolean isReserved() {
        return resultCode == PointResultCode.RESERVED;
    }

    /**
     * 예약 실패 여부를 확인한다.
     */
    public boolean isFailed() {
        return resultCode == PointResultCode.FAILED;
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

    public long getRequestedPointAmount() {
        return requestedPointAmount;
    }

    public long getReservedPointAmount() {
        return reservedPointAmount;
    }

    public PointResultCode getResultCode() {
        return resultCode;
    }

    public PointFailureReason getFailureReason() {
        return failureReason;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
