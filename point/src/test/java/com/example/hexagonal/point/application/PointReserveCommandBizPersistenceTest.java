package com.example.hexagonal.point.application;

import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.point.PointMySqlTestcontainersConfig;
import com.example.hexagonal.PointTestApplication;
import com.example.hexagonal.point.adapters.point.out.persistence.PointAccountPersistenceAdapter;
import com.example.hexagonal.point.adapters.point.out.persistence.PointReservationEntity;
import com.example.hexagonal.point.adapters.point.out.persistence.PointReservationJpaRepository;
import com.example.hexagonal.point.adapters.point.out.persistence.PointReservationPersistenceAdapter;
import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.example.hexagonal.point.application.port.in.ReservePointCommand;
import com.example.hexagonal.point.application.port.out.PointAccountCommandPort;
import com.example.hexagonal.point.application.port.out.PointAccountQueryPort;
import com.example.hexagonal.point.application.port.out.PointResultPublishPort;
import com.example.hexagonal.point.domain.PointAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * 포인트 예약 Biz가 JPA 저장과 멱등 처리를 함께 수행하는지 검증한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PointTestApplication.class)
@EntityScan("com.example.hexagonal.point.adapters.point.out.persistence")
@EnableJpaRepositories("com.example.hexagonal.point.adapters.point.out.persistence")
@Import({
        PointMySqlTestcontainersConfig.class,
        PointAccountPersistenceAdapter.class,
        PointReservationPersistenceAdapter.class,
        PointReserveCommandBizImpl.class
})
class PointReserveCommandBizPersistenceTest {

    @Autowired
    private PointReserveCommandBiz pointReserveCommandBiz;

    @Autowired
    private PointAccountQueryPort pointAccountQueryPort;

    @Autowired
    private PointAccountCommandPort pointAccountCommandPort;

    @Autowired
    private PointReservationJpaRepository pointReservationJpaRepository;

    @MockBean
    private PointResultPublishPort pointResultPublishPort;

    @Test
    void reserve_whenAccountHasEnoughPoints_thenDeductsBalanceAndStoresReservation() {
        Instant now = Instant.parse("2026-04-10T10:00:00Z");
        pointAccountCommandPort.save(PointAccount.create("user-1", 1_000L, now));

        pointReserveCommandBiz.reserve(command("order-1", "benefit-order-1", "user-1", 300L, 2_000L, now));

        PointReservationEntity reservation = pointReservationJpaRepository.findByBenefitRequestId("benefit-order-1").orElseThrow();
        assertThat(pointAccountQueryPort.findByUserId("user-1").orElseThrow().getAvailablePointAmount()).isEqualTo(700L);
        assertThat(reservation.getStatus()).isEqualTo("RESERVED");
        assertThat(reservation.getReservedPointAmount()).isEqualTo(300L);
        verify(pointResultPublishPort).publishReserved("order-1", "benefit-order-1", "user-1", 300L, 300L);
    }

    @Test
    void reserve_whenPointsAreInsufficient_thenStoresFailureAndPublishesFailedResult() {
        Instant now = Instant.parse("2026-04-10T10:00:00Z");
        pointAccountCommandPort.save(PointAccount.create("user-1", 100L, now));

        pointReserveCommandBiz.reserve(command("order-1", "benefit-order-1", "user-1", 300L, 2_000L, now));

        PointReservationEntity reservation = pointReservationJpaRepository.findByBenefitRequestId("benefit-order-1").orElseThrow();
        assertThat(pointAccountQueryPort.findByUserId("user-1").orElseThrow().getAvailablePointAmount()).isEqualTo(100L);
        assertThat(reservation.getStatus()).isEqualTo("FAILED");
        assertThat(reservation.getFailureReason()).isEqualTo(PointFailureReason.INSUFFICIENT_POINTS.getCode());
        verify(pointResultPublishPort).publishFailed(
                "order-1",
                "benefit-order-1",
                "user-1",
                300L,
                PointFailureReason.INSUFFICIENT_POINTS,
                "Not enough points for user: user-1"
        );
    }

    @Test
    void reserve_whenDuplicateCommandArrives_thenRepublishesExistingResultWithoutDeductingAgain() {
        Instant now = Instant.parse("2026-04-10T10:00:00Z");
        pointAccountCommandPort.save(PointAccount.create("user-1", 1_000L, now));
        ReservePointCommand command = command("order-1", "benefit-order-1", "user-1", 300L, 2_000L, now);

        pointReserveCommandBiz.reserve(command);
        reset(pointResultPublishPort);

        pointReserveCommandBiz.reserve(command);

        assertThat(pointAccountQueryPort.findByUserId("user-1").orElseThrow().getAvailablePointAmount()).isEqualTo(700L);
        assertThat(pointReservationJpaRepository.findAll()).hasSize(1);
        verify(pointResultPublishPort).publishReserved("order-1", "benefit-order-1", "user-1", 300L, 300L);
    }

    /**
     * 테스트에서 반복해서 쓰는 예약 명령을 만든다.
     */
    private ReservePointCommand command(String orderId,
                                        String benefitRequestId,
                                        String userId,
                                        long requestedPointAmount,
                                        long payableAmount,
                                        Instant createdAt) {
        return new ReservePointCommand(orderId, benefitRequestId, userId, requestedPointAmount, payableAmount, createdAt);
    }
}
