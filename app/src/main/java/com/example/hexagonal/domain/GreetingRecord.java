package com.example.hexagonal.domain;

import java.time.Instant;

public class GreetingRecord {
    private final Long id;
    private final String message;
    private final Instant createdAt;

    public GreetingRecord(Long id, String message, Instant createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
