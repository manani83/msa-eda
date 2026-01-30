package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.adapters.in.web.GlobalExceptionHandler;
import com.example.hexagonal.application.order.port.in.CreateOrderUseCase;
import com.example.hexagonal.domain.coupon.CouponNotFoundException;
import com.example.hexagonal.domain.coupon.CouponValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void create_order_returns_error_when_coupon_missing() throws Exception {
        given(createOrderUseCase.create(org.mockito.ArgumentMatchers.any()))
                .willThrow(new CouponNotFoundException("NOPE"));

        String payload = """
                {
                  \"userId\": \"user-1\",
                  \"items\": [{\"productId\": \"prod-1\", \"quantity\": 1, \"unitPrice\": 1000}],
                  \"shippingAddress\": {\"zip\": \"12345\", \"line1\": \"line1\", \"line2\": \"line2\"},
                  \"couponCode\": \"NOPE\"
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COUPON_NOT_FOUND"));
    }

    @Test
    void create_order_returns_error_when_coupon_invalid() throws Exception {
        given(createOrderUseCase.create(org.mockito.ArgumentMatchers.any()))
                .willThrow(new CouponValidationException("Coupon is not valid at this time: EXPIRED10"));

        String payload = """
                {
                  \"userId\": \"user-1\",
                  \"items\": [{\"productId\": \"prod-1\", \"quantity\": 1, \"unitPrice\": 1000}],
                  \"shippingAddress\": {\"zip\": \"12345\", \"line1\": \"line1\", \"line2\": \"line2\"},
                  \"couponCode\": \"EXPIRED10\"
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COUPON_INVALID"));
    }
}
