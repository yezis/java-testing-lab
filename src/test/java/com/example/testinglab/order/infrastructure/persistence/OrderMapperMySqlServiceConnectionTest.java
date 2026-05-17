package com.example.testinglab.order.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.testinglab.support.MySqlServiceConnectionTestBase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperMySqlServiceConnectionTest extends MySqlServiceConnectionTestBase {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldInsertAndSelectOrderWithServiceConnection() {
        OrderDO order = new OrderDO();
        order.setProductId("p-service-1001");
        order.setProductName("Service Connection Book");
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("88.00"));

        orderMapper.insert(order);

        OrderDO savedOrder = orderMapper.selectById("p-service-1001");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getProductName()).isEqualTo("Service Connection Book");
        assertThat(savedOrder.getQuantity()).isEqualTo(1);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("88.00");
    }
}
