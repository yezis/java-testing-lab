package com.example.testinglab.order.interfaces.rest;

import com.example.testinglab.common.error.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerRandomPortTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldGetOrderByIdThroughRealHttpPort() {
        OrderResponse response = testRestTemplate.getForObject("http://localhost:" + port + "/api/orders/o-1001", OrderResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("o-1001");
        assertThat(response.productName()).isEqualTo("Java Testing Book");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("119.80");
    }

    @Test
    void shouldReturnNotFoundThroughRealHttpPort() {
        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity("http://localhost:" + port + "/api/orders/o-404", ErrorResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ORDER_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("order not found: o-404");
    }

}
