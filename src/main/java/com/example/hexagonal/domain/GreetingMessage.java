package com.example.hexagonal.domain;

public class GreetingMessage {
    private final String value;

    public GreetingMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
