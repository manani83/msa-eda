package com.example.hexagonal.point.adapters.point.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * 포인트 예약 결과를 DB에 저장하기 위한 영속성 엔티티다.
 */
@Entity
@Table(
        name = "point_reservations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_point_reservations_benefit_request_id", columnNames = "benefit_request_id")
        },
        indexes = {
                @Index(name = "idx_point_reservations_order_id", columnList = "order_id"),
                @Index(name = "idx_point_reservations_user_id", columnList = "user_id")
        }
)
public class PointReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benefit_request_id", nullable = false, length = 64)
    private String benefitRequestId;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "requested_point_amount", nullable = false)
    private long requestedPointAmount;

    @Column(name = "reserved_point_amount", nullable = false)
    private long reservedPointAmount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PointReservationEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getBenefitRequestId() {
        return benefitRequestId;
    }

    public void setBenefitRequestId(String benefitRequestId) {
        this.benefitRequestId = benefitRequestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getRequestedPointAmount() {
        return requestedPointAmount;
    }

    public void setRequestedPointAmount(long requestedPointAmount) {
        this.requestedPointAmount = requestedPointAmount;
    }

    public long getReservedPointAmount() {
        return reservedPointAmount;
    }

    public void setReservedPointAmount(long reservedPointAmount) {
        this.reservedPointAmount = reservedPointAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
