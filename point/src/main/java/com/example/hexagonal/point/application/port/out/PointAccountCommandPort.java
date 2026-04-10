package com.example.hexagonal.point.application.port.out;

import com.example.hexagonal.point.domain.PointAccount;

/**
 * 포인트 계정의 변경 내용을 저장하는 포트다.
 */
public interface PointAccountCommandPort {
    /**
     * 계정 잔액 변경 결과를 저장한다.
     */
    PointAccount save(PointAccount account);
}
