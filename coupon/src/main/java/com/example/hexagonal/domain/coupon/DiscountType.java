package com.example.hexagonal.domain.coupon;

import java.util.Arrays;

public enum DiscountType {
    PERCENT("PERCENT", "정률 할인"),
    FIXED_AMOUNT("FIXED_AMOUNT", "정액 할인");

    private final String code;
    private final String description;

    DiscountType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DiscountType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown DiscountType code: " + code));
    }
}
