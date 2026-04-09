package com.example.hexagonal.adapters.order.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutboxEntity, Long> {
    Optional<OrderOutboxEntity> findFirstByAggregateId(String aggregateId);

    List<OrderOutboxEntity> findByAggregateIdOrderByIdAsc(String aggregateId);

    @Query("""
            select outbox
            from OrderOutboxEntity outbox
            where outbox.status in :statuses
              and outbox.nextAttemptAt <= :now
            order by outbox.id asc
            """)
    List<OrderOutboxEntity> findPublishable(@Param("statuses") List<String> statuses,
                                            @Param("now") Instant now,
                                            Pageable pageable);
}
