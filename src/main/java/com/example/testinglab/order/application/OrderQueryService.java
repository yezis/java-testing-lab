package com.example.testinglab.order.application;

import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderNotFoundException;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {

    public Order findById(String orderId) {
        if ("o-404".equals(orderId)) {
            throw new OrderNotFoundException(orderId);
        }

        return new Order(orderId, "Java Testing Book", 2, new BigDecimal("119.80"));
    }
}
