package com.example.hexagonal.point.application.port.out;

import com.example.hexagonal.point.domain.PointAccount;

import java.util.Optional;

/**
 * 포인트 계정 조회를 담당하는 포트다.
 */
public interface PointAccountQueryPort {
    /**
     * 사용자 ID로 포인트 계정을 찾는다.
     */
    Optional<PointAccount> findByUserId(String userId);
}
