package com.example.hexagonal.domain.coupon;

import java.util.Map;

public enum DiscountType {
    PERCENT("PERCENT", "정률 할인"),
    FIXED_AMOUNT("FIXED_AMOUNT", "정액 할인");

    private static final Map<String, DiscountType> BY_CODE = Map.of(
            PERCENT.code, PERCENT,
            FIXED_AMOUNT.code, FIXED_AMOUNT
    );

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
        DiscountType discountType = BY_CODE.get(code);
        if (discountType == null) {
            throw new IllegalArgumentException("Unknown DiscountType code: " + code);
        }
        return discountType;
    }
}
