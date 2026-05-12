package com.example.testinglab.order.interfaces.rest;

import com.example.testinglab.order.application.OrderCommandService;
import com.example.testinglab.order.application.OrderQueryService;
import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderQueryService orderQueryService;

    @MockBean
    private OrderCommandService orderCommandService;

    @Test
    void shouldGetOrderById() throws Exception {
        Order order = new Order("o-1001", "Java Testing Book", 2, new BigDecimal("119.80"));

        when(orderQueryService.findById("o-1001")).thenReturn(order);

        mockMvc.perform(get("/api/orders/o-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("o-1001"))
                .andExpect(jsonPath("$.productName").value("Java Testing Book"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(119.80));

        verify(orderQueryService).findById("o-1001");
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderQueryService.findById("o-404")).thenThrow(new OrderNotFoundException("o-404"));

        mockMvc.perform(get("/api/orders/o-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("order not found: o-404"));

        verify(orderQueryService).findById("o-404");
    }

    @Test
    void shouldReturnBadRequestWhenOrderIdFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/orders/1-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("orderId must match pattern o-{number}")));

        verifyNoInteractions(orderQueryService);
    }

    @Test
    void shouldCreateOrderWhenRequestBodyIsValid() throws Exception {
        Order order = new Order("o-1001", "Java Testing Book", 2, new BigDecimal("119.80"));

        when(orderCommandService.createOrder("p-1001", 2)).thenReturn(order);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "productId": "p-1001",
                            "quantity": 2
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("o-1001"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.productName").value("Java Testing Book"))
                .andExpect(jsonPath("$.totalAmount").value(119.80));


        verify(orderCommandService).createOrder("p-1001", 2);
    }

    @Test
    void shouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "productId": "p-1001",
                            "quantity": 0
                         }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("quantity must be greater than or equal to 1")));

        verifyNoInteractions(orderCommandService);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyTypeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productId": "p-1001",
                            "quantity": "abc"
                         }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("request body is not readable")));

        verifyNoInteractions(orderCommandService);
    }

}

