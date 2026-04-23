package com.example.hexagonal.point.adapters.point.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 포인트 계정 JPA 저장소다.
 */
public interface PointAccountJpaRepository extends JpaRepository<PointAccountEntity, String> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update PointAccountEntity account
               set account.availablePointAmount = :availablePointAmount,
                   account.updatedAt = :updatedAt
             where account.userId = :userId
            """)
    int updateBalance(@Param("userId") String userId,
                      @Param("availablePointAmount") long availablePointAmount,
                      @Param("updatedAt") Instant updatedAt);
}
