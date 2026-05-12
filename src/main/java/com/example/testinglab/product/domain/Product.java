package com.example.testinglab.product.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

    private final String id;
    private final String name;
    private final BigDecimal price;
    private final int stock;

    public Product(String id, String name, BigDecimal price, int stock) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.price = Objects.requireNonNull(price, "price must not be null");
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
