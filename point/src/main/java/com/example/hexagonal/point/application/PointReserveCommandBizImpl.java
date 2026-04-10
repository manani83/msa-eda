package com.example.hexagonal.point.application;

import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.point.application.port.in.PointReserveCommandBiz;
import com.example.hexagonal.point.application.port.in.ReservePointCommand;
import com.example.hexagonal.point.application.port.out.PointAccountCommandPort;
import com.example.hexagonal.point.application.port.out.PointAccountQueryPort;
import com.example.hexagonal.point.application.port.out.PointReservationCommandPort;
import com.example.hexagonal.point.application.port.out.PointReservationQueryPort;
import com.example.hexagonal.point.application.port.out.PointResultPublishPort;
import com.example.hexagonal.point.domain.PointAccount;
import com.example.hexagonal.point.domain.PointReservation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 포인트 예약 요청을 검증하고 계정 차감과 결과 발행을 조합한다.
 */
@Service
public class PointReserveCommandBizImpl implements PointReserveCommandBiz {
    private final PointAccountQueryPort pointAccountQueryPort;
    private final PointAccountCommandPort pointAccountCommandPort;
    private final PointReservationQueryPort pointReservationQueryPort;
    private final PointReservationCommandPort pointReservationCommandPort;
    private final PointResultPublishPort pointResultPublishPort;

    public PointReserveCommandBizImpl(PointAccountQueryPort pointAccountQueryPort,
                                      PointAccountCommandPort pointAccountCommandPort,
                                      PointReservationQueryPort pointReservationQueryPort,
                                      PointReservationCommandPort pointReservationCommandPort,
                                      PointResultPublishPort pointResultPublishPort) {
        this.pointAccountQueryPort = pointAccountQueryPort;
        this.pointAccountCommandPort = pointAccountCommandPort;
        this.pointReservationQueryPort = pointReservationQueryPort;
        this.pointReservationCommandPort = pointReservationCommandPort;
        this.pointResultPublishPort = pointResultPublishPort;
    }

    /**
     * 중복 수신, 잔액 검증, 저장, 결과 발행까지 한 번에 처리한다.
     */
    @Override
    @Transactional
    public void reserve(ReservePointCommand command) {
        validate(command);

        Optional<PointReservation> existingReservation = pointReservationQueryPort.findByBenefitRequestId(command.benefitRequestId());
        if (existingReservation.isPresent()) {
            publish(existingReservation.get());
            return;
        }

        if (command.requestedPointAmount() <= 0 || command.payableAmount() < 0 || command.payableAmount() < command.requestedPointAmount()) {
            saveFailure(command, PointFailureReason.INVALID_AMOUNT, "Invalid point reservation amount");
            return;
        }

        PointAccount account = pointAccountQueryPort.findByUserId(command.userId()).orElse(null);
        if (account == null) {
            saveFailure(command, PointFailureReason.ACCOUNT_NOT_FOUND, "Point account not found: " + command.userId());
            return;
        }

        if (account.getAvailablePointAmount() < command.requestedPointAmount()) {
            saveFailure(command, PointFailureReason.INSUFFICIENT_POINTS, "Not enough points for user: " + command.userId());
            return;
        }

        PointReservation reservation = PointReservation.reserved(
                command.benefitRequestId(),
                command.orderId(),
                command.userId(),
                command.requestedPointAmount(),
                command.requestedPointAmount(),
                command.createdAt()
        );

        try {
            pointReservationCommandPort.save(reservation);
        } catch (DataIntegrityViolationException exception) {
            pointReservationQueryPort.findByBenefitRequestId(command.benefitRequestId())
                    .ifPresentOrElse(this::publish, () -> { throw exception; });
            return;
        }

        PointAccount updatedAccount = account.deduct(command.requestedPointAmount(), command.createdAt());
        pointAccountCommandPort.save(updatedAccount);
        pointResultPublishPort.publishReserved(
                command.orderId(),
                command.benefitRequestId(),
                command.userId(),
                command.requestedPointAmount(),
                command.requestedPointAmount()
        );
    }

    /**
     * 포인트 예약에 필요한 필수 입력값을 먼저 확인한다.
     */
    private void validate(ReservePointCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.orderId() == null || command.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (command.benefitRequestId() == null || command.benefitRequestId().isBlank()) {
            throw new IllegalArgumentException("benefitRequestId must not be blank");
        }
        if (command.userId() == null || command.userId().isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (command.createdAt() == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
    }

    /**
     * 실패한 예약도 이력으로 남기고 결과 이벤트를 발행한다.
     */
    private void saveFailure(ReservePointCommand command, PointFailureReason reason, String reasonMessage) {
        long requestedPointAmount = Math.max(command.requestedPointAmount(), 0);
        PointReservation failedReservation = PointReservation.failed(
                command.benefitRequestId(),
                command.orderId(),
                command.userId(),
                requestedPointAmount,
                reason,
                reasonMessage,
                command.createdAt()
        );
        try {
            pointReservationCommandPort.save(failedReservation);
        } catch (DataIntegrityViolationException exception) {
            pointReservationQueryPort.findByBenefitRequestId(command.benefitRequestId())
                    .ifPresentOrElse(this::publish, () -> { throw exception; });
            return;
        }
        pointResultPublishPort.publishFailed(
                command.orderId(),
                command.benefitRequestId(),
                command.userId(),
                requestedPointAmount,
                reason,
                reasonMessage
        );
    }

    /**
     * 이미 저장된 예약 결과를 다시 읽어 결과 이벤트만 재발행한다.
     */
    private void publish(PointReservation reservation) {
        if (reservation.isReserved()) {
            pointResultPublishPort.publishReserved(
                    reservation.getOrderId(),
                    reservation.getBenefitRequestId(),
                    reservation.getUserId(),
                    reservation.getRequestedPointAmount(),
                    reservation.getReservedPointAmount()
            );
            return;
        }
        pointResultPublishPort.publishFailed(
                reservation.getOrderId(),
                reservation.getBenefitRequestId(),
                reservation.getUserId(),
                reservation.getRequestedPointAmount(),
                reservation.getFailureReason(),
                reservation.getFailureMessage()
        );
    }
}
