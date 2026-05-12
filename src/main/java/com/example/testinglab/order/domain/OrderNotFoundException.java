package com.example.testinglab.order.domain;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("order not found: " + orderId);
    }
}
