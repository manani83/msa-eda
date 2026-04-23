package com.example.hexagonal.application.order;

import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.coupon.CouponAppliedEvent;
import com.example.hexagonal.application.event.coupon.CouponApplyCommandEvent;
import com.example.hexagonal.application.event.coupon.CouponRejectedEvent;
import com.example.hexagonal.application.event.order.OrderEventTopics;
import com.example.hexagonal.application.event.point.PointFailedEvent;
import com.example.hexagonal.application.event.point.PointReserveCommandEvent;
import com.example.hexagonal.application.event.point.PointReservedEvent;
import com.example.hexagonal.application.order.port.out.OrderBenefitSagaCommandPort;
import com.example.hexagonal.application.order.port.out.OrderBenefitSagaQueryPort;
import com.example.hexagonal.application.order.port.out.OrderCommandPort;
import com.example.hexagonal.application.order.port.out.OrderQueryPort;
import com.example.hexagonal.application.order.port.out.OutboxCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxMessage;
import com.example.hexagonal.domain.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderSagaOrchestrator {
    private static final String ORDER_EVENT_PRODUCER = "order-service";
    private static final String ORDER_AGGREGATE_TYPE = "order";
    private static final int ORDER_EVENT_SCHEMA_VERSION = 1;
    private static final String COUPON_APPLY_COMMAND_EVENT_TYPE = "coupon.apply.command";
    private static final String POINT_RESERVE_COMMAND_EVENT_TYPE = "point.reserve.command";

    private final OrderCommandPort orderCommandPort;
    private final OrderQueryPort orderQueryPort;
    private final OrderBenefitSagaCommandPort orderBenefitSagaCommandPort;
    private final OrderBenefitSagaQueryPort orderBenefitSagaQueryPort;
    private final OutboxCommandPort outboxCommandPort;

    public OrderSagaOrchestrator(OrderCommandPort orderCommandPort,
                                 OrderQueryPort orderQueryPort,
                                 OrderBenefitSagaCommandPort orderBenefitSagaCommandPort,
                                 OrderBenefitSagaQueryPort orderBenefitSagaQueryPort,
                                 OutboxCommandPort outboxCommandPort) {
        this.orderCommandPort = orderCommandPort;
        this.orderQueryPort = orderQueryPort;
        this.orderBenefitSagaCommandPort = orderBenefitSagaCommandPort;
        this.orderBenefitSagaQueryPort = orderBenefitSagaQueryPort;
        this.outboxCommandPort = outboxCommandPort;
    }

    @Transactional
    // 주문 생성 직후 첫 사가 상태를 만들고 다음 command를 결정한다.
    public Order start(Order order, long requestedPointAmount) {
        OrderBenefitSaga saga = orderBenefitSagaCommandPort.save(
                OrderBenefitSaga.start(buildBenefitRequestId(order.getOrderId()), order, requestedPointAmount)
        );

        if (!saga.hasCouponRequest() && !saga.hasPointRequest()) {
            orderBenefitSagaCommandPort.save(saga.completeWithoutBenefits());
            return orderCommandPort.save(order.completeBenefits());
        }

        if (saga.hasCouponRequest()) {
            enqueueCouponApplyCommand(order, saga);
            return order;
        }

        enqueuePointReserveCommand(order, saga);
        return order;
    }

    @Transactional
    // 쿠폰 적용 성공 결과를 반영하고 필요하면 포인트 command를 이어서 보낸다.
    public void handleCouponApplied(EventEnvelope<CouponAppliedEvent> envelope) {
        OrderBenefitSaga saga = getSaga(envelope.correlationId());
        Order order = getOrder(envelope.aggregateId());

        Order updatedOrder = order.applyCouponDiscount(envelope.payload().discountAmount(), saga.hasPointRequest());
        Order savedOrder = orderCommandPort.save(updatedOrder);
        OrderBenefitSaga updatedSaga = orderBenefitSagaCommandPort.save(saga.couponApplied(envelope.payload().discountAmount()));

        if (updatedSaga.hasPointRequest()) {
            enqueuePointReserveCommand(savedOrder, updatedSaga);
        }
    }

    @Transactional
    // 쿠폰 적용 실패 결과를 반영하고 주문을 실패 상태로 마감한다.
    public void handleCouponRejected(EventEnvelope<CouponRejectedEvent> envelope) {
        OrderBenefitSaga saga = getSaga(envelope.correlationId());
        Order order = getOrder(envelope.aggregateId());

        orderCommandPort.save(order.markBenefitsFailed());
        orderBenefitSagaCommandPort.save(saga.couponRejected(envelope.payload().reasonCode().getCode()));
    }

    @Transactional
    // 포인트 예약 성공 결과를 반영하고 주문을 최종 완료한다.
    public void handlePointReserved(EventEnvelope<PointReservedEvent> envelope) {
        OrderBenefitSaga saga = getSaga(envelope.correlationId());
        Order order = getOrder(envelope.aggregateId());

        orderCommandPort.save(order.applyPointReservation(envelope.payload().reservedPointAmount()));
        orderBenefitSagaCommandPort.save(saga.pointReserved(envelope.payload().reservedPointAmount()));
    }

    @Transactional
    // 포인트 예약 실패 결과를 반영하고 실패 유형에 맞게 주문 상태를 정리한다.
    public void handlePointFailed(EventEnvelope<PointFailedEvent> envelope) {
        OrderBenefitSaga saga = getSaga(envelope.correlationId());
        Order order = getOrder(envelope.aggregateId());

        Order failedOrder = saga.getCouponStatus() == CouponProcessStatus.APPLIED
                ? order.markPartialFailed()
                : order.markBenefitsFailed();
        orderCommandPort.save(failedOrder);
        orderBenefitSagaCommandPort.save(saga.pointFailed(envelope.payload().reasonCode().getCode()));
    }

    // 쿠폰 서비스에 전달할 적용 command를 outbox에 적재한다.
    private void enqueueCouponApplyCommand(Order order, OrderBenefitSaga saga) {
        long subtotalAmount = order.getTotalAmount().getAmount();
        CouponApplyCommandEvent payload = new CouponApplyCommandEvent(
                order.getOrderId(),
                saga.getBenefitRequestId(),
                order.getUserId(),
                order.getCouponCode(),
                subtotalAmount,
                order.getCreatedAt()
        );
        outboxCommandPort.save(OutboxMessage.pending(
                OrderEventTopics.COUPON_APPLY_COMMAND_V1,
                OrderEventTopics.messageKey(order.getOrderId()),
                envelope(
                        COUPON_APPLY_COMMAND_EVENT_TYPE,
                        order.getOrderId(),
                        saga.getBenefitRequestId(),
                        order.getCreatedAt(),
                        payload
                )
        ));
    }

    // 포인트 서비스에 전달할 예약 command를 outbox에 적재한다.
    private void enqueuePointReserveCommand(Order order, OrderBenefitSaga saga) {
        PointReserveCommandEvent payload = new PointReserveCommandEvent(
                order.getOrderId(),
                saga.getBenefitRequestId(),
                order.getUserId(),
                saga.getRequestedPointAmount(),
                order.getTotalAmount().getAmount(),
                order.getCreatedAt()
        );
        outboxCommandPort.save(OutboxMessage.pending(
                OrderEventTopics.POINT_RESERVE_COMMAND_V1,
                OrderEventTopics.messageKey(order.getOrderId()),
                envelope(
                        POINT_RESERVE_COMMAND_EVENT_TYPE,
                        order.getOrderId(),
                        saga.getBenefitRequestId(),
                        order.getCreatedAt(),
                        payload
                )
        ));
    }

    // 공통 이벤트 메타데이터를 감싼 envelope를 만든다.
    private <T> EventEnvelope<T> envelope(String eventType,
                                          String orderId,
                                          String benefitRequestId,
                                          Instant occurredAt,
                                          T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                ORDER_EVENT_SCHEMA_VERSION,
                occurredAt,
                ORDER_EVENT_PRODUCER,
                ORDER_AGGREGATE_TYPE,
                orderId,
                benefitRequestId,
                orderId,
                payload
        );
    }

    // 상관관계 키로 현재 진행 중인 사가를 조회한다.
    private OrderBenefitSaga getSaga(String benefitRequestId) {
        return orderBenefitSagaQueryPort.findByBenefitRequestId(benefitRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found: " + benefitRequestId));
    }

    // 결과를 반영할 대상 주문을 조회한다.
    private Order getOrder(String orderId) {
        return orderQueryPort.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    // 주문 기준으로 사가 상관관계 키를 만든다.
    private String buildBenefitRequestId(String orderId) {
        return "benefit-" + orderId;
    }
}
