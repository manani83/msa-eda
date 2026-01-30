package com.example.hexagonal.adapters.in.web;

import com.example.hexagonal.application.GreetingQueryUseCase;
import com.example.hexagonal.application.GreetingUseCase;
import com.example.hexagonal.domain.GreetingMessage;
import com.example.hexagonal.domain.GreetingRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GreetingController {
    private final GreetingUseCase greetingUseCase;
    private final GreetingQueryUseCase greetingQueryUseCase;

    public GreetingController(GreetingUseCase greetingUseCase, GreetingQueryUseCase greetingQueryUseCase) {
        this.greetingUseCase = greetingUseCase;
        this.greetingQueryUseCase = greetingQueryUseCase;
    }

    @GetMapping("/hello")
    public String hello() {
        GreetingMessage message = greetingUseCase.greet();
        return message.getValue();
    }

    @GetMapping("/greetings")
    public List<GreetingRecordResponse> list() {
        List<GreetingRecord> records = greetingQueryUseCase.list();
        return records.stream()
                .map(record -> new GreetingRecordResponse(record.getId(), record.getMessage(), record.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public static class GreetingRecordResponse {
        private final Long id;
        private final String message;
        private final Instant createdAt;

        public GreetingRecordResponse(Long id, String message, Instant createdAt) {
            this.id = id;
            this.message = message;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }
}
