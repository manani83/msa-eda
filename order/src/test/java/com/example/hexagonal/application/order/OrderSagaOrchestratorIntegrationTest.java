package com.example.hexagonal.application.order;

import com.example.hexagonal.OrderIntegrationTestApplication;
import com.example.hexagonal.OrderMySqlTestcontainersConfig;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderBenefitSagaJpaRepository;
import com.example.hexagonal.adapters.order.out.persistence.OrderJpaRepository;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxEntity;
import com.example.hexagonal.adapters.order.out.persistence.OrderOutboxJpaRepository;
import com.example.hexagonal.application.event.common.EventEnvelope;
import com.example.hexagonal.application.event.coupon.CouponAppliedEvent;
import com.example.hexagonal.application.event.coupon.CouponFailureReason;
import com.example.hexagonal.application.event.coupon.CouponRejectedEvent;
import com.example.hexagonal.application.event.coupon.CouponResultCode;
import com.example.hexagonal.application.event.point.PointFailedEvent;
import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.point.PointReservedEvent;
import com.example.hexagonal.application.event.point.PointResultCode;
import com.example.hexagonal.application.order.port.in.CreateOrderCommand;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.domain.order.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = OrderIntegrationTestApplication.class,
        properties = "order.saga.listener.enabled=false"
)
@Import(OrderMySqlTestcontainersConfig.class)
@Transactional
class OrderSagaOrchestratorIntegrationTest {

    @Autowired
    private CreateOrderBiz createOrderBiz;

    @Autowired
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderBenefitSagaJpaRepository orderBenefitSagaJpaRepository;

    @Autowired
    private OrderOutboxJpaRepository orderOutboxJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void handle_coupon_applied_when_point_requested_then_updates_order_and_enqueues_point_command() {
        CreateOrderResult result = createOrderBiz.create(command("WELCOME10", 300L));
        List<Long> itemIdsBefore = orderItemIds(result.getOrderId());
        OrderBenefitSagaEntity createdSaga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();

        orderSagaOrchestrator.handleCouponApplied(envelope(
                "coupon.applied",
                result.getOrderId(),
                createdSaga.getBenefitRequestId(),
                new CouponAppliedEvent(result.getOrderId(), createdSaga.getBenefitRequestId(), "WELCOME10", 200L, CouponResultCode.APPLIED)
        ));

        List<Long> itemIdsAfter = orderItemIds(result.getOrderId());
        OrderBenefitSagaEntity updatedSaga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();
        OrderOutboxEntity latestOutbox = orderOutboxJpaRepository.findByAggregateIdOrderByIdAsc(result.getOrderId()).get(1);

        assertThat(itemIdsAfter).containsExactlyElementsOf(itemIdsBefore);
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING_BENEFITS.getCode());
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getDiscountAmount()).isEqualTo(200L);
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getTotalAmount()).isEqualTo(1800L);
        assertThat(updatedSaga.getCurrentStep()).isEqualTo(OrderSagaStep.WAITING_POINT_RESULT.getCode());
        assertThat(updatedSaga.getCouponStatus()).isEqualTo(CouponProcessStatus.APPLIED.getCode());
        assertThat(latestOutbox.getTopic()).isEqualTo("point.reserve.command.v1");
        assertThat(latestOutbox.getPayloadJson()).contains("\"requestedPointAmount\":300");
        assertThat(latestOutbox.getPayloadJson()).contains("\"payableAmount\":1800");
    }

    @Test
    void handle_point_reserved_after_coupon_then_completes_order() {
        CreateOrderResult result = createOrderBiz.create(command("WELCOME10", 300L));
        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();

        orderSagaOrchestrator.handleCouponApplied(envelope(
                "coupon.applied",
                result.getOrderId(),
                saga.getBenefitRequestId(),
                new CouponAppliedEvent(result.getOrderId(), saga.getBenefitRequestId(), "WELCOME10", 200L, CouponResultCode.APPLIED)
        ));
        orderSagaOrchestrator.handlePointReserved(envelope(
                "point.reserved",
                result.getOrderId(),
                saga.getBenefitRequestId(),
                new PointReservedEvent(result.getOrderId(), saga.getBenefitRequestId(), "user-1", 300L, 300L, PointResultCode.RESERVED)
        ));

        OrderBenefitSagaEntity completedSaga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.BENEFITS_COMPLETED.getCode());
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getDiscountAmount()).isEqualTo(500L);
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getTotalAmount()).isEqualTo(1500L);
        assertThat(completedSaga.getCurrentStep()).isEqualTo(OrderSagaStep.COMPLETED.getCode());
        assertThat(completedSaga.getPointStatus()).isEqualTo(PointProcessStatus.RESERVED.getCode());
    }

    @Test
    void handle_coupon_rejected_then_marks_order_failed() {
        CreateOrderResult result = createOrderBiz.create(command("NOPE", null));
        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();

        orderSagaOrchestrator.handleCouponRejected(envelope(
                "coupon.rejected",
                result.getOrderId(),
                saga.getBenefitRequestId(),
                new CouponRejectedEvent(
                        result.getOrderId(),
                        saga.getBenefitRequestId(),
                        "NOPE",
                        CouponResultCode.REJECTED,
                        CouponFailureReason.NOT_FOUND,
                        "Coupon not found"
                )
        ));

        OrderBenefitSagaEntity failedSaga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.BENEFITS_FAILED.getCode());
        assertThat(failedSaga.getCurrentStep()).isEqualTo(OrderSagaStep.FAILED.getCode());
        assertThat(failedSaga.getCouponStatus()).isEqualTo(CouponProcessStatus.REJECTED.getCode());
        assertThat(failedSaga.getLastFailureCode()).isEqualTo(CouponFailureReason.NOT_FOUND.getCode());
    }

    @Test
    void handle_point_failed_after_coupon_then_marks_order_partial_failed() {
        CreateOrderResult result = createOrderBiz.create(command("WELCOME10", 300L));
        OrderBenefitSagaEntity saga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();

        orderSagaOrchestrator.handleCouponApplied(envelope(
                "coupon.applied",
                result.getOrderId(),
                saga.getBenefitRequestId(),
                new CouponAppliedEvent(result.getOrderId(), saga.getBenefitRequestId(), "WELCOME10", 200L, CouponResultCode.APPLIED)
        ));
        orderSagaOrchestrator.handlePointFailed(envelope(
                "point.failed",
                result.getOrderId(),
                saga.getBenefitRequestId(),
                new PointFailedEvent(
                        result.getOrderId(),
                        saga.getBenefitRequestId(),
                        "user-1",
                        300L,
                        PointResultCode.FAILED,
                        PointFailureReason.INSUFFICIENT_POINTS,
                        "Not enough points"
                )
        ));

        OrderBenefitSagaEntity failedSaga = orderBenefitSagaJpaRepository.findByOrderId(result.getOrderId()).orElseThrow();
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PARTIAL_FAILED.getCode());
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getDiscountAmount()).isEqualTo(200L);
        assertThat(orderJpaRepository.findById(result.getOrderId()).orElseThrow().getTotalAmount()).isEqualTo(1800L);
        assertThat(failedSaga.getCurrentStep()).isEqualTo(OrderSagaStep.FAILED.getCode());
        assertThat(failedSaga.getPointStatus()).isEqualTo(PointProcessStatus.FAILED.getCode());
        assertThat(failedSaga.getLastFailureCode()).isEqualTo(PointFailureReason.INSUFFICIENT_POINTS.getCode());
    }

    private CreateOrderCommand command(String couponCode, Long pointAmount) {
        return new CreateOrderCommand(
                "user-1",
                List.of(new CreateOrderCommand.CreateOrderItem("prod-1", 2, 1000)),
                new CreateOrderCommand.CreateOrderAddress("12345", "line1", "line2"),
                couponCode,
                pointAmount
        );
    }

    private <T> EventEnvelope<T> envelope(String eventType, String orderId, String benefitRequestId, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                1,
                Instant.parse("2026-04-02T00:00:00Z"),
                "test",
                "order",
                orderId,
                benefitRequestId,
                orderId,
                payload
        );
    }

    private List<Long> orderItemIds(String orderId) {
        entityManager.flush();
        entityManager.clear();
        List<?> rows = entityManager.createNativeQuery(
                        "select id from order_items where order_id = :orderId order by id")
                .setParameter("orderId", orderId)
                .getResultList();
        return rows.stream()
                .map(row -> ((Number) row).longValue())
                .toList();
    }
}
