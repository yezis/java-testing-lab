package com.example.testinglab.order.application;

import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderCalculator;
import com.example.testinglab.order.domain.OrderRepository;
import com.example.testinglab.product.domain.Product;
import com.example.testinglab.product.domain.ProductRepository;

import java.math.BigDecimal;

public class OrderService {

    private final ProductRepository productRepository;
    private final OrderCalculator orderCalculator;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository, OrderCalculator orderCalculator, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderCalculator = orderCalculator;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("product stock is not enough");
        }

        BigDecimal totalAmount = orderCalculator.calculateTotal(product.getPrice(), quantity);

        Order order = new Order(product.getId(), product.getName(), quantity, totalAmount);

        return orderRepository.save(order);
    }
}
