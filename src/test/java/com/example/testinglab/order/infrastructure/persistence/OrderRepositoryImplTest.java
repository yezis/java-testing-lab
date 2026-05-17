package com.example.testinglab.order.infrastructure.persistence;

import com.example.testinglab.common.model.PageResult;
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

    @Test
    void shouldFindOrdersByProductNameAndMinTotalAmount() {
        Order order1 = new Order("p-5001", "MyBatis Plus Book", 1, new BigDecimal("50.00"));
        Order order2 = new Order("p-5002", "MyBatis Plus Book", 2, new BigDecimal("100.00"));
        Order order3 = new Order("p-5003", "MyBatis Plus Book", 3, new BigDecimal("150.00"));
        Order order4 = new Order("p-5004", "Java Testing Book", 4, new BigDecimal("200.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        List<Order> orders = orderRepository.findByProductNameAndMinTotalAmount("MyBatis Plus Book", new BigDecimal("100.00"));

        assertThat(orders).hasSize(2);
        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactlyInAnyOrder("p-5002", "p-5003");
    }

    @Test
    void shouldFindOrdersByProductNameOrderByTotalAmountDesc() {
        Order order1 = new Order("p-6001", "MyBatis Plus Book", 1, new BigDecimal("100.00"));
        Order order2 = new Order("p-6002", "MyBatis Plus Book", 3, new BigDecimal("300.00"));
        Order order3 = new Order("p-6003", "MyBatis Plus Book", 2, new BigDecimal("200.00"));
        Order order4 = new Order("p-6004", "Java Testing Book", 9, new BigDecimal("999.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        List<Order> orders = orderRepository.findByProductNameOrderByTotalAmountDesc("MyBatis Plus Book");

        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactly("p-6002", "p-6003", "p-6001");
    }

    @Test
    void shouldFindOrdersByProductNameLike() {
        Order order1 = new Order("p-7001", "MyBatis Plus Book", 1, new BigDecimal("100.00"));
        Order order2 = new Order("p-7002", "MyBatis Plus Testing Guide", 2, new BigDecimal("200.00"));
        Order order3 = new Order("p-7003", "Java Testing Book", 3, new BigDecimal("300.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> orders = orderRepository.findByProductNameLike("MyBatis Plus");

        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactlyInAnyOrder("p-7001", "p-7002");
    }

    @Test
    void shouldFindOrdersByTotalAmountBetweenOrderByTotalAmountAsc() {
        Order order1 = new Order("p-8001", "Book A", 1, new BigDecimal("50.00"));
        Order order2 = new Order("p-8002", "Book B", 2, new BigDecimal("100.00"));
        Order order3 = new Order("p-8003", "Book C", 3, new BigDecimal("200.00"));
        Order order4 = new Order("p-8004", "Book D", 4, new BigDecimal("300.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        List<Order> orders = orderRepository.findByTotalAmountBetweenOrderByTotalAmountAsc(
                new BigDecimal("100.00"),
                new BigDecimal("250.00")
        );

        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactly("p-8002", "p-8003");
    }

    @Test
    void shouldCountOrdersByProductName() {
        Order order1 = new Order("p-8101", "MyBatis Plus Book", 1, new BigDecimal("100.00"));
        Order order2 = new Order("p-8102", "MyBatis Plus Book", 2, new BigDecimal("200.00"));
        Order order3 = new Order("p-8103", "Java Testing Book", 3, new BigDecimal("300.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        long count = orderRepository.countByProductName("MyBatis Plus Book");

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindOrdersByProductNamePageOrderByTotalAmountAsc() {
        Order order1 = new Order("p-8201", "MyBatis Plus Book", 1, new BigDecimal("100.00"));
        Order order2 = new Order("p-8202", "MyBatis Plus Book", 2, new BigDecimal("200.00"));
        Order order3 = new Order("p-8203", "MyBatis Plus Book", 3, new BigDecimal("300.00"));
        Order order4 = new Order("p-8204", "Java Testing Book", 4, new BigDecimal("400.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        PageResult<Order> page = orderRepository.findByProductNamePageOrderByTotalAmountAsc(
                "MyBatis Plus Book",
                2,
                2
        );

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.current()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.pages()).isEqualTo(2);
        assertThat(page.records())
                .extracting(Order::getProductId)
                .containsExactly("p-8203");
    }

    @Test
    void shouldSearchOrdersWithDynamicConditions() {
        Order order1 = new Order("p-8301", "MyBatis Plus Book", 1, new BigDecimal("80.00"));
        Order order2 = new Order("p-8302", "MyBatis Plus Book", 2, new BigDecimal("150.00"));
        Order order3 = new Order("p-8303", "MyBatis Plus Book", 3, new BigDecimal("250.00"));
        Order order4 = new Order("p-8304", "Java Testing Book", 4, new BigDecimal("180.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);

        List<Order> orders = orderRepository.search("MyBatis Plus Book", new BigDecimal("100.00"), new BigDecimal("200.00"));

        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactly("p-8302");
    }

    @Test
    void shouldIgnoreNullDynamicConditions() {
        Order order1 = new Order("p-8401", "MyBatis Plus Book", 1, new BigDecimal("80.00"));
        Order order2 = new Order("p-8402", "Java Testing Book", 2, new BigDecimal("150.00"));
        Order order3 = new Order("p-8403", "Spring Boot Book", 3, new BigDecimal("250.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        List<Order> orders = orderRepository.search(null, new BigDecimal("100.00"), null);

        assertThat(orders)
                .extracting(Order::getProductId)
                .containsExactly("p-8402", "p-8403");
    }

    @Test
    void shouldUpdateQuantityByProductName() {
        Order order1 = new Order("p-8501", "MyBatis Plus Book", 1, new BigDecimal("80.00"));
        Order order2 = new Order("p-8502", "MyBatis Plus Book", 2, new BigDecimal("160.00"));
        Order order3 = new Order("p-8503", "Java Testing Book", 3, new BigDecimal("240.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        int updatedRows = orderRepository.updateQuantityByProductName("MyBatis Plus Book", 10);

        assertThat(updatedRows).isEqualTo(2);
        assertThat(orderMapper.selectById("p-8501").getQuantity()).isEqualTo(10);
        assertThat(orderMapper.selectById("p-8502").getQuantity()).isEqualTo(10);
        assertThat(orderMapper.selectById("p-8503").getQuantity()).isEqualTo(3);
    }

    @Test
    void shouldDeleteOrdersByProductName() {
        Order order1 = new Order("p-8601", "MyBatis Plus Book", 1, new BigDecimal("80.00"));
        Order order2 = new Order("p-8602", "MyBatis Plus Book", 2, new BigDecimal("160.00"));
        Order order3 = new Order("p-8603", "Java Testing Book", 3, new BigDecimal("240.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        int deletedRows = orderRepository.deleteByProductName("MyBatis Plus Book");

        assertThat(deletedRows).isEqualTo(2);
        assertThat(orderMapper.selectById("p-8601")).isNull();
        assertThat(orderMapper.selectById("p-8602")).isNull();
        assertThat(orderMapper.selectById("p-8603")).isNotNull();
    }
}
