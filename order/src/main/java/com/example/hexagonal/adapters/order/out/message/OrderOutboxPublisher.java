package com.example.hexagonal.adapters.order.out.message;

import com.example.hexagonal.application.order.port.out.OutboxCommandPort;
import com.example.hexagonal.application.order.port.out.OutboxQueryPort;
import com.example.hexagonal.application.order.port.out.OutboxRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class OrderOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private final OutboxQueryPort outboxQueryPort;
    private final OutboxCommandPort outboxCommandPort;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderOutboxPublisher(OutboxQueryPort outboxQueryPort,
                                OutboxCommandPort outboxCommandPort,
                                KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxCommandPort = outboxCommandPort;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 발행 가능한 outbox 이벤트를 배치 단위로 읽어 Kafka로 보낸다.
    public int publishPending(int batchSize) {
        List<OutboxRecord> publishable = outboxQueryPort.findPublishable(batchSize, Instant.now());
        for (OutboxRecord outboxRecord : publishable) {
            publish(outboxRecord);
        }
        return publishable.size();
    }

    // 한 건의 outbox 이벤트를 Kafka로 보내고 결과 상태를 갱신한다.
    private void publish(OutboxRecord outboxRecord) {
        Instant now = Instant.now();
        try {
            kafkaTemplate.send(
                    outboxRecord.topic(),
                    outboxRecord.messageKey(),
                    outboxRecord.payloadJson()
            ).get();
            outboxCommandPort.markPublished(outboxRecord.id(), now);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(outboxRecord, now, e);
        } catch (ExecutionException e) {
            handleFailure(outboxRecord, now, e.getCause() == null ? e : e.getCause());
        } catch (Exception e) {
            handleFailure(outboxRecord, now, e);
        }
    }

    // 발행 실패 정보를 남기고 재시도 가능 시각을 계산한다.
    private void handleFailure(OutboxRecord outboxRecord, Instant now, Throwable throwable) {
        log.warn(
                "Failed to publish outbox event. outboxId={}, eventId={}, aggregateId={}, reason={}",
                outboxRecord.id(),
                outboxRecord.eventId(),
                outboxRecord.aggregateId(),
                throwable.getMessage()
        );
        outboxCommandPort.markFailed(
                outboxRecord.id(),
                nextAttemptAt(outboxRecord.retryCount(), now),
                throwable.getMessage()
        );
    }

    // 재시도 횟수에 따라 다음 발행 시각을 점진적으로 늦춘다.
    private Instant nextAttemptAt(int retryCount, Instant now) {
        long delaySeconds = Math.min(300L, 10L * (1L << Math.min(retryCount, 4)));
        return now.plusSeconds(delaySeconds);
    }
}
