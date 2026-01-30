package com.example.hexagonal.application;

import com.example.hexagonal.application.port.out.GreetingRecordPort;
import com.example.hexagonal.application.port.out.GreetingRecordQueryPort;
import com.example.hexagonal.domain.GreetingMessage;
import com.example.hexagonal.domain.GreetingRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GreetingService implements GreetingUseCase, GreetingQueryUseCase {
    private final GreetingRecordPort greetingRecordPort;
    private final GreetingRecordQueryPort greetingRecordQueryPort;

    public GreetingService(GreetingRecordPort greetingRecordPort, GreetingRecordQueryPort greetingRecordQueryPort) {
        this.greetingRecordPort = greetingRecordPort;
        this.greetingRecordQueryPort = greetingRecordQueryPort;
    }

    @Override
    public GreetingMessage greet() {
        GreetingMessage message = new GreetingMessage("hello world");
        greetingRecordPort.save(message.getValue());
        return message;
    }

    @Override
    public List<GreetingRecord> list() {
        return greetingRecordQueryPort.findAll();
    }
}
