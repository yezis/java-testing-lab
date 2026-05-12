package com.example.testinglab.order.application;

import com.example.testinglab.order.domain.Order;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class OrderCommandService {

    public Order createOrder(String productId, int quantity) {
        return new Order(productId, "Java Testing Book", quantity, new BigDecimal("119.80"));
    }
}
