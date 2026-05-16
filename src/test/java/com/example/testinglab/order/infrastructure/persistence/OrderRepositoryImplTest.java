package com.example.testinglab.order.infrastructure.persistence;

import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderRepositoryImplTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldSaveOrderThroughMyBatisPlusMapper() {
        Order order = new Order("p-2001", "MyBatis Plus Book", 3, new BigDecimal("177.00"));

        Order savedOrder = orderRepository.save(order);

        OrderDO orderDO = orderMapper.selectById("p-2001");
        assertThat(savedOrder.getProductId()).isEqualTo("p-2001");
        assertThat(orderDO).isNotNull();
        assertThat(orderDO.getProductName()).isEqualTo("MyBatis Plus Book");
        assertThat(orderDO.getQuantity()).isEqualTo(3);
        assertThat(orderDO.getTotalAmount()).isEqualByComparingTo("177.00");
    }

    @Test
    void shouldFindOrdersByProductName() {
        Order order1 = new Order("p-3001", "MyBatis Plus Book", 3, new BigDecimal("177.00"));
        Order order2 = new Order("p-3002", "MyBatis Plus Book", 3, new BigDecimal("177.00"));
        Order order3 = new Order("p-3003", "MyBatis Plus Test Book", 3, new BigDecimal("177.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> orders = orderRepository.findByProductName("MyBatis Plus Book");
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getProductName).containsOnly("MyBatis Plus Book");
    }

    @Test
    void shouldReturnEmptyListWhenProductNameDoesNotExist() {
        Order order = new Order("p-4001", "Java Testing Book", 1, new BigDecimal("59.00"));
        orderRepository.save(order);

        List<Order> orders = orderRepository.findByProductName("Unknown Book");

        assertThat(orders).isEmpty();
    }
}
