package com.example.testinglab.order.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldInsertAndSelectOrderDO() {
        OrderDO order = new OrderDO();
        order.setProductId("p-3001");
        order.setProductName("MyBatis Plus Book");
        order.setQuantity(3);
        order.setTotalAmount(new BigDecimal("177.00"));

        orderMapper.insert(order);

        OrderDO savedOrderDO = orderMapper.selectById("p-3001");
        assertThat(savedOrderDO).isNotNull();
        assertThat(savedOrderDO.getProductId()).isEqualTo("p-3001");
        assertThat(savedOrderDO.getProductName()).isEqualTo("MyBatis Plus Book");
        assertThat(savedOrderDO.getQuantity()).isEqualTo(3);
        assertThat(savedOrderDO.getTotalAmount()).isEqualByComparingTo("177.00");
    }
}
