package com.example.testinglab.order;

import java.math.BigDecimal;

public record OrderResponse(
        String orderId,
        String productName,
        int quantity,
        BigDecimal totalAmount
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount()
        );
    }
}
