package com.example.testinglab.order.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerSpringBootTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetOrderByIdWithFullSpringContext() throws Exception {
        mockMvc.perform(get("/api/orders/o-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("o-1001"))
                .andExpect(jsonPath("$.productName").value("Java Testing Book"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(119.80));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/orders/o-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("order not found: o-404"));

    }

    @Test
    void shouldCreateOrderWhenRequestBodyIsValid() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": "p-1001",
                            "quantity": 2
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("p-1001"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.productName").value("Java Testing Book"))
                .andExpect(jsonPath("$.totalAmount").value(119.80));
    }
}
