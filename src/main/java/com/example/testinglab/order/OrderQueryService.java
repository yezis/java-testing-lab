package com.example.testinglab.order;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {

    public Order findById(String orderId) {
        return new Order(orderId, "Java Testing Book", 2, new BigDecimal("119.80"));
    }
}
