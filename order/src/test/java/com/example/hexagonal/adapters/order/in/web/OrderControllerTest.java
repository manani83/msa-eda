package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.OrderMySqlTestcontainersConfig;
import com.example.hexagonal.OrderTestApplication;
import com.example.hexagonal.application.order.CreateOrderBiz;
import com.example.hexagonal.application.order.port.in.CreateOrderResult;
import com.example.hexagonal.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderTestApplication.class)
@AutoConfigureMockMvc
@Import({OrderController.class, OrderTestExceptionHandler.class, OrderMySqlTestcontainersConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderBiz createOrderBiz;

    @Test
    void create_order_returns_pending_response() throws Exception {
        given(createOrderBiz.create(org.mockito.ArgumentMatchers.any()))
                .willReturn(new CreateOrderResult(
                        "20260326000001",
                        OrderStatus.PENDING_BENEFITS,
                        "WELCOME10",
                        0,
                        2000,
                        Instant.parse("2026-03-26T01:00:00Z")
                ));

        String payload = """
                {
                  \"userId\": \"user-1\",
                  \"items\": [{\"productId\": \"prod-1\", \"quantity\": 2, \"unitPrice\": 1000}],
                  \"shippingAddress\": {\"zip\": \"12345\", \"line1\": \"line1\", \"line2\": \"line2\"},
                  \"couponCode\": \"WELCOME10\"
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("20260326000001"))
                .andExpect(jsonPath("$.status").value("PENDING_BENEFITS"))
                .andExpect(jsonPath("$.couponCode").value("WELCOME10"))
                .andExpect(jsonPath("$.discountAmount").value(0))
                .andExpect(jsonPath("$.totalAmount").value(2000));
    }

    @Test
    void create_order_returns_bad_request_when_items_missing() throws Exception {
        String payload = """
                {
                  \"userId\": \"user-1\",
                  \"shippingAddress\": {\"zip\": \"12345\", \"line1\": \"line1\", \"line2\": \"line2\"}
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_order_returns_bad_request_when_user_id_blank() throws Exception {
        String payload = """
                {
                  \"userId\": \" \",
                  \"items\": [{\"productId\": \"prod-1\", \"quantity\": 1, \"unitPrice\": 1000}],
                  \"shippingAddress\": {\"zip\": \"12345\", \"line1\": \"line1\", \"line2\": \"line2\"}
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
