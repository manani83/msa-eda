package com.example.hexagonal.application.event.order;

public final class OrderEventTopics {
    public static final String ORDER_CREATED_V1 = "order.created.v1";
    public static final String COUPON_APPLY_COMMAND_V1 = "coupon.apply.command.v1";
    public static final String COUPON_APPLY_RESULT_V1 = "coupon.apply.result.v1";
    public static final String POINT_RESERVE_COMMAND_V1 = "point.reserve.command.v1";
    public static final String POINT_RESERVE_RESULT_V1 = "point.reserve.result.v1";

    private OrderEventTopics() {
    }

    public static String messageKey(String orderId) {
        return orderId;
    }
}
