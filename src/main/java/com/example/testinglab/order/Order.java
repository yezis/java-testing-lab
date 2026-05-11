package com.example.testinglab.order;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {

    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal totalAmount;

    public Order(String productId, String productName, int quantity, BigDecimal totalAmount) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.productName = Objects.requireNonNull(productName, "productName must not be null");
        this.quantity = quantity;
        this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
