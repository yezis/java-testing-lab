package com.example.testinglab.order.domain;

import java.util.List;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findByProductName(String productName);
}
