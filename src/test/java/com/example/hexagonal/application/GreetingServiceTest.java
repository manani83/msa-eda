package com.example.hexagonal.application;

import com.example.hexagonal.application.port.out.GreetingRecordPort;
import com.example.hexagonal.domain.GreetingMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceTest {

    @Test
    void greet_returns_message_and_saves_record() {
        FakeGreetingRecordPort fakePort = new FakeGreetingRecordPort();
        GreetingService service = new GreetingService(fakePort, () -> java.util.List.of());

        GreetingMessage result = service.greet();

        assertThat(result.getValue()).isEqualTo("hello world");
        assertThat(fakePort.savedCount).isEqualTo(1);
        assertThat(fakePort.lastMessage).isEqualTo("hello world");
    }

    private static class FakeGreetingRecordPort implements GreetingRecordPort {
        int savedCount;
        String lastMessage;

        @Override
        public void save(String message) {
            savedCount++;
            lastMessage = message;
        }
    }
}
