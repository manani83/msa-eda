package com.example.hexagonal.point.adapters.point.out.persistence;

import com.example.hexagonal.point.application.port.out.PointAccountCommandPort;
import com.example.hexagonal.point.application.port.out.PointAccountQueryPort;
import com.example.hexagonal.point.domain.PointAccount;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 포인트 계정 도메인과 JPA 엔티티를 서로 변환한다.
 */
@Component
public class PointAccountPersistenceAdapter implements PointAccountQueryPort, PointAccountCommandPort {
    private final PointAccountJpaRepository pointAccountJpaRepository;

    public PointAccountPersistenceAdapter(PointAccountJpaRepository pointAccountJpaRepository) {
        this.pointAccountJpaRepository = pointAccountJpaRepository;
    }

    /**
     * 사용자 ID로 포인트 계정을 조회한다.
     */
    @Override
    public Optional<PointAccount> findByUserId(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return pointAccountJpaRepository.findById(userId).map(this::toDomain);
    }

    /**
     * 계정 변경 내용을 저장하고 도메인으로 되돌린다.
     */
    @Override
    public PointAccount save(PointAccount account) {
        return toDomain(pointAccountJpaRepository.save(toEntity(account)));
    }

    /**
     * 기존 계정의 잔액만 직접 갱신한다.
     */
    @Override
    public PointAccount update(PointAccount account) {
        int updatedRows = pointAccountJpaRepository.updateBalance(
                account.getUserId(),
                account.getAvailablePointAmount(),
                account.getUpdatedAt()
        );
        if (updatedRows == 0) {
            throw new IllegalArgumentException("Point account not found: " + account.getUserId());
        }
        return account;
    }

    /**
     * JPA 엔티티를 도메인 객체로 바꾼다.
     */
    private PointAccount toDomain(PointAccountEntity entity) {
        return new PointAccount(
                entity.getUserId(),
                entity.getAvailablePointAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * 도메인 객체를 JPA 엔티티로 바꾼다.
     */
    private PointAccountEntity toEntity(PointAccount account) {
        PointAccountEntity entity = new PointAccountEntity();
        entity.setUserId(account.getUserId());
        entity.setAvailablePointAmount(account.getAvailablePointAmount());
        entity.setCreatedAt(account.getCreatedAt());
        entity.setUpdatedAt(account.getUpdatedAt());
        return entity;
    }
}
