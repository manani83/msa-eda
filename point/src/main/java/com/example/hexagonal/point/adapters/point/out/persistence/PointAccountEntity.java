package com.example.hexagonal.point.adapters.point.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 포인트 계정의 영속성 저장 형태다.
 */
@Entity
@Table(name = "point_accounts")
public class PointAccountEntity {
    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "available_point_amount", nullable = false)
    private long availablePointAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PointAccountEntity() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getAvailablePointAmount() {
        return availablePointAmount;
    }

    public void setAvailablePointAmount(long availablePointAmount) {
        this.availablePointAmount = availablePointAmount;
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
