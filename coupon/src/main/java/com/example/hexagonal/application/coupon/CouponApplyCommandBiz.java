package com.example.hexagonal.application.coupon;

import com.example.hexagonal.application.coupon.port.in.ApplyCouponCommand;

public interface CouponApplyCommandBiz {
    void apply(ApplyCouponCommand command);
}
