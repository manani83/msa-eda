package com.example.hexagonal.application;

import com.example.hexagonal.domain.GreetingMessage;

public interface GreetingUseCase {
    GreetingMessage greet();
}
