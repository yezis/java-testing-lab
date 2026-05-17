package com.example.testinglab.order.domain;

import com.example.testinglab.common.model.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findByProductName(String productName);

    List<Order> findByProductNameAndMinTotalAmount(String productName, BigDecimal minTotalAmount);

    List<Order> findByProductNameOrderByTotalAmountDesc(String productName);

    List<Order> findByProductNameLike(String keyword);

    List<Order> findByTotalAmountBetweenOrderByTotalAmountAsc(BigDecimal minTotalAmount, BigDecimal maxTotalAmount);

    long countByProductName(String productName);

    PageResult<Order> findByProductNamePageOrderByTotalAmountAsc(String productName, long pageNumber, long pageSize);

    List<Order> search(String productName, BigDecimal minTotalAmount, BigDecimal maxTotalAmount);

    int updateQuantityByProductName(String productName, int quantity);

    int deleteByProductName(String productName);
}
