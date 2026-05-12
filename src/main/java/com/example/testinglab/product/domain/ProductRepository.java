package com.example.testinglab.product.domain;

import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(String productId);
}
