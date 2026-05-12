package com.example.testinglab.order.interfaces.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "productId must not be blank")
        String productId,

        @Min(value = 1, message = "quantity must be greater than or equal to 1")
        int quantity
) {
}
