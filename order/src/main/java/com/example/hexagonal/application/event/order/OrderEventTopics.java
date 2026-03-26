package com.example.hexagonal.application.event.order;

public final class OrderEventTopics {
    public static final String ORDER_CREATED_V1 = "order.created.v1";
    public static final String COUPON_RESULT_V1 = "coupon.result.v1";
    public static final String POINT_RESULT_V1 = "point.result.v1";

    private OrderEventTopics() {
    }

    public static String messageKey(String orderId) {
        return orderId;
    }
}
