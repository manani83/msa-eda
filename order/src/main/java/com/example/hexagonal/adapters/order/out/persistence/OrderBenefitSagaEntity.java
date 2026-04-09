package com.example.hexagonal.adapters.order.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(
        name = "order_benefit_saga",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_benefit_saga_request_id", columnNames = "benefit_request_id"),
                @UniqueConstraint(name = "uk_order_benefit_saga_order_id", columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_order_benefit_saga_order_id", columnList = "order_id")
        }
)
public class OrderBenefitSagaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "benefit_request_id", nullable = false, length = 64)
    private String benefitRequestId;

    @Column(name = "order_id", nullable = false, length = 14)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "requested_point_amount", nullable = false)
    private long requestedPointAmount;

    @Column(name = "current_step", nullable = false, length = 40)
    private String currentStep;

    @Column(name = "coupon_status", nullable = false, length = 30)
    private String couponStatus;

    @Column(name = "point_status", nullable = false, length = 30)
    private String pointStatus;

    @Column(name = "coupon_discount_amount", nullable = false)
    private long couponDiscountAmount;

    @Column(name = "reserved_point_amount", nullable = false)
    private long reservedPointAmount;

    @Column(name = "last_failure_code", length = 100)
    private String lastFailureCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected OrderBenefitSagaEntity() {
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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public long getRequestedPointAmount() {
        return requestedPointAmount;
    }

    public void setRequestedPointAmount(long requestedPointAmount) {
        this.requestedPointAmount = requestedPointAmount;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getCouponStatus() {
        return couponStatus;
    }

    public void setCouponStatus(String couponStatus) {
        this.couponStatus = couponStatus;
    }

    public String getPointStatus() {
        return pointStatus;
    }

    public void setPointStatus(String pointStatus) {
        this.pointStatus = pointStatus;
    }

    public long getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(long couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public long getReservedPointAmount() {
        return reservedPointAmount;
    }

    public void setReservedPointAmount(long reservedPointAmount) {
        this.reservedPointAmount = reservedPointAmount;
    }

    public String getLastFailureCode() {
        return lastFailureCode;
    }

    public void setLastFailureCode(String lastFailureCode) {
        this.lastFailureCode = lastFailureCode;
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
