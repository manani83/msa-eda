package com.example.hexagonal.adapters.out.persistence;

import com.example.hexagonal.application.port.out.GreetingRecordPort;
import com.example.hexagonal.application.port.out.GreetingRecordQueryPort;
import com.example.hexagonal.domain.GreetingRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GreetingRecordPersistenceAdapter implements GreetingRecordPort, GreetingRecordQueryPort {
    private final GreetingRecordJpaRepository repository;

    public GreetingRecordPersistenceAdapter(GreetingRecordJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(String message) {
        repository.save(new GreetingRecordEntity(message));
    }

    @Override
    public List<GreetingRecord> findAll() {
        return repository.findAll()
                .stream()
                .map(entity -> new GreetingRecord(entity.getId(), entity.getMessage(), entity.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
