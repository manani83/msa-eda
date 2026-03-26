package com.example.hexagonal.domain.coupon;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountTypeTest {

    @Test
    void discountType_containsCodeAndDescription() {
        assertThat(DiscountType.PERCENT.getCode()).isEqualTo("PERCENT");
        assertThat(DiscountType.PERCENT.getDescription()).isEqualTo("정률 할인");
        assertThat(DiscountType.FIXED_AMOUNT.getCode()).isEqualTo("FIXED_AMOUNT");
        assertThat(DiscountType.FIXED_AMOUNT.getDescription()).isEqualTo("정액 할인");
    }

    @Test
    void fromCode_whenKnownCode_thenReturnsMatchingType() {
        assertThat(DiscountType.fromCode("PERCENT")).isEqualTo(DiscountType.PERCENT);
        assertThat(DiscountType.fromCode("FIXED_AMOUNT")).isEqualTo(DiscountType.FIXED_AMOUNT);
    }
}
