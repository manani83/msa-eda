package com.example.hexagonal.adapters.order.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface OrderSequenceJpaRepository extends JpaRepository<OrderSequenceEntity, String> {
    @Modifying
    @Query(value = "merge into order_sequences (date_prefix, next_seq) key(date_prefix) values (:datePrefix, 2)", nativeQuery = true)
    void initSequence(@Param("datePrefix") String datePrefix);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from OrderSequenceEntity s where s.datePrefix = :datePrefix")
    Optional<OrderSequenceEntity> findForUpdate(@Param("datePrefix") String datePrefix);
}
