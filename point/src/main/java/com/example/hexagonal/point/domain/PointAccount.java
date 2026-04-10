package com.example.hexagonal.point.domain;

import java.time.Instant;

/**
 * 사용자별 포인트 잔액과 변경 시점을 담는 도메인 객체다.
 */
public class PointAccount {
    private final String userId;
    private final long availablePointAmount;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PointAccount(String userId, long availablePointAmount, Instant createdAt, Instant updatedAt) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (availablePointAmount < 0) {
            throw new IllegalArgumentException("availablePointAmount must be >= 0");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("createdAt and updatedAt must not be null");
        }
        this.userId = userId;
        this.availablePointAmount = availablePointAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 포인트 계정을 생성한다.
     */
    public static PointAccount create(String userId, long availablePointAmount, Instant now) {
        return new PointAccount(userId, availablePointAmount, now, now);
    }

    /**
     * 보유 포인트를 차감한 새 상태를 반환한다.
     */
    public PointAccount deduct(long amount, Instant updatedAt) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
        if (amount > availablePointAmount) {
            throw new IllegalArgumentException("amount must be <= availablePointAmount");
        }
        return new PointAccount(userId, availablePointAmount - amount, createdAt, updatedAt);
    }

    public String getUserId() {
        return userId;
    }

    public long getAvailablePointAmount() {
        return availablePointAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
