package com.example.hexagonal.domain.order;

public class Money {
    private final long amount;

    private Money(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        this.amount = amount;
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money multiply(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        return new Money(this.amount * quantity);
    }

    public long getAmount() {
        return amount;
    }
}
