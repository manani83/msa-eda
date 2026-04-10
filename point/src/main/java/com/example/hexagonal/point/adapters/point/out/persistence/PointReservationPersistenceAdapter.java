package com.example.hexagonal.point.adapters.point.out.persistence;

import com.example.hexagonal.application.event.point.PointFailureReason;
import com.example.hexagonal.application.event.point.PointResultCode;
import com.example.hexagonal.point.application.port.out.PointReservationCommandPort;
import com.example.hexagonal.point.application.port.out.PointReservationQueryPort;
import com.example.hexagonal.point.domain.PointReservation;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 포인트 예약 결과를 JPA 엔티티와 도메인 사이에서 변환한다.
 */
@Component
public class PointReservationPersistenceAdapter implements PointReservationQueryPort, PointReservationCommandPort {
    private final PointReservationJpaRepository pointReservationJpaRepository;

    public PointReservationPersistenceAdapter(PointReservationJpaRepository pointReservationJpaRepository) {
        this.pointReservationJpaRepository = pointReservationJpaRepository;
    }

    /**
     * benefitRequestId로 예약 이력을 조회한다.
     */
    @Override
    public Optional<PointReservation> findByBenefitRequestId(String benefitRequestId) {
        if (benefitRequestId == null) {
            return Optional.empty();
        }
        return pointReservationJpaRepository.findByBenefitRequestId(benefitRequestId).map(this::toDomain);
    }

    /**
     * 포인트 예약 결과를 저장하고 도메인으로 되돌린다.
     */
    @Override
    public PointReservation save(PointReservation reservation) {
        return toDomain(pointReservationJpaRepository.save(toEntity(reservation)));
    }

    /**
     * 예약 엔티티를 도메인 객체로 바꾼다.
     */
    private PointReservation toDomain(PointReservationEntity entity) {
        PointResultCode resultCode = PointResultCode.fromCode(entity.getStatus());
        PointFailureReason failureReason = entity.getFailureReason() == null ? null : PointFailureReason.fromCode(entity.getFailureReason());
        return PointReservation.rehydrate(
                entity.getBenefitRequestId(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getRequestedPointAmount(),
                entity.getReservedPointAmount(),
                resultCode,
                failureReason,
                entity.getFailureMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 도메인 예약 결과를 JPA 엔티티로 바꾼다.
     */
    private PointReservationEntity toEntity(PointReservation reservation) {
        PointReservationEntity entity = new PointReservationEntity();
        entity.setBenefitRequestId(reservation.getBenefitRequestId());
        entity.setOrderId(reservation.getOrderId());
        entity.setUserId(reservation.getUserId());
        entity.setRequestedPointAmount(reservation.getRequestedPointAmount());
        entity.setReservedPointAmount(reservation.getReservedPointAmount());
        entity.setStatus(reservation.getResultCode().getCode());
        entity.setFailureReason(reservation.getFailureReason() == null ? null : reservation.getFailureReason().getCode());
        entity.setFailureMessage(reservation.getFailureMessage());
        entity.setCreatedAt(reservation.getCreatedAt());
        entity.setUpdatedAt(reservation.getUpdatedAt());
        return entity;
    }
}
