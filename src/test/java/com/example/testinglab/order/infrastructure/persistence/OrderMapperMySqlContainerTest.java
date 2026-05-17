package com.example.testinglab.order.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class OrderMapperMySqlContainerTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("testing_lab")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> "60000");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldInsertAndSelectOrderWithRealMySqlContainer() {
        OrderDO order = new OrderDO();
        order.setProductId("p-mysql-1001");
        order.setProductName("MySQL Container Book");
        order.setQuantity(2);
        order.setTotalAmount(new BigDecimal("198.00"));

        orderMapper.insert(order);

        OrderDO savedOrder = orderMapper.selectById("p-mysql-1001");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getProductId()).isEqualTo("p-mysql-1001");
        assertThat(savedOrder.getProductName()).isEqualTo("MySQL Container Book");
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("198.00");
    }
}
