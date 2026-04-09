package com.example.hexagonal.application.order.port.out;

import java.time.Instant;
import java.util.List;

public interface OutboxQueryPort {
    List<OutboxRecord> findPublishable(int limit, Instant now);
}
