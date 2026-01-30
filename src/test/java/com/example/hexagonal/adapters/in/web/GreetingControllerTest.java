package com.example.hexagonal.adapters.in.web;

import com.example.hexagonal.application.GreetingQueryUseCase;
import com.example.hexagonal.application.GreetingUseCase;
import com.example.hexagonal.domain.GreetingMessage;
import com.example.hexagonal.domain.GreetingRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GreetingUseCase greetingUseCase;

    @MockBean
    private GreetingQueryUseCase greetingQueryUseCase;

    @Test
    void hello_returns_plain_text_message() throws Exception {
        given(greetingUseCase.greet()).willReturn(new GreetingMessage("hello world"));

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }

    @Test
    void greetings_returns_json_list() throws Exception {
        Instant now = Instant.parse("2026-01-28T09:00:00Z");
        given(greetingQueryUseCase.list()).willReturn(List.of(new GreetingRecord(1L, "hello world", now)));

        mockMvc.perform(get("/greetings"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\":1,\"message\":\"hello world\",\"createdAt\":\"2026-01-28T09:00:00Z\"}]"));
    }
}
