package com.example.testinglab.order;

import java.math.BigDecimal;

public class OrderCalculator {

    public BigDecimal calculateTotal(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
