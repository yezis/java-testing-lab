package com.example.testinglab.order.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void shouldRejectDuplicateProductId() {
        OrderDO firstOrder = new OrderDO();
        firstOrder.setProductId("p-9001");
        firstOrder.setProductName("MyBatis Plus Book");
        firstOrder.setQuantity(1);
        firstOrder.setTotalAmount(new BigDecimal("99.00"));

        OrderDO duplicateOrder = new OrderDO();
        duplicateOrder.setProductId("p-9001");
        duplicateOrder.setProductName("Java Testing Book");
        duplicateOrder.setQuantity(2);
        duplicateOrder.setTotalAmount(new BigDecimal("199.00"));

        orderMapper.insert(firstOrder);

        assertThatThrownBy(() -> orderMapper.insert(duplicateOrder))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldRejectNullProductName() {
        OrderDO order = new OrderDO();
        order.setProductId("p-9002");
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("99.00"));

        assertThatThrownBy(() -> orderMapper.insert(order))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldUpdateOrderById() {
        OrderDO order = new OrderDO();
        order.setProductId("p-9101");
        order.setProductName("Old Book");
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("50.00"));
        orderMapper.insert(order);

        OrderDO updateOrder = new OrderDO();
        updateOrder.setProductId("p-9101");
        updateOrder.setProductName("Updated Book");
        updateOrder.setQuantity(2);
        updateOrder.setTotalAmount(new BigDecimal("120.00"));

        int affectedRows = orderMapper.updateById(updateOrder);

        OrderDO savedOrder = orderMapper.selectById("p-9101");
        assertThat(affectedRows).isEqualTo(1);
        assertThat(savedOrder.getProductName()).isEqualTo("Updated Book");
        assertThat(savedOrder.getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void shouldDeleteOrderById() {
        OrderDO order = new OrderDO();
        order.setProductId("p-9102");
        order.setProductName("Book To Delete");
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("80.00"));
        orderMapper.insert(order);

        int affectedRows = orderMapper.deleteById("p-9102");

        OrderDO deletedOrder = orderMapper.selectById("p-9102");
        assertThat(affectedRows).isEqualTo(1);
        assertThat(deletedOrder).isNull();
    }

    @Test
    void shouldSumTotalAmountByProductNameWithCustomSql() {
        OrderDO order1 = new OrderDO();
        order1.setProductId("p-9201");
        order1.setProductName("MyBatis Plus Book");
        order1.setQuantity(1);
        order1.setTotalAmount(new BigDecimal("100.00"));

        OrderDO order2 = new OrderDO();
        order2.setProductId("p-9202");
        order2.setProductName("MyBatis Plus Book");
        order2.setQuantity(2);
        order2.setTotalAmount(new BigDecimal("200.00"));

        OrderDO order3 = new OrderDO();
        order3.setProductId("p-9203");
        order3.setProductName("Java Testing Book");
        order3.setQuantity(3);
        order3.setTotalAmount(new BigDecimal("300.00"));

        orderMapper.insert(order1);
        orderMapper.insert(order2);
        orderMapper.insert(order3);

        BigDecimal totalAmount = orderMapper.sumTotalAmountByProductName("MyBatis Plus Book");

        assertThat(totalAmount).isEqualByComparingTo("300.00");
    }

    @Test
    void shouldSelectOrdersByBatchIds() {
        OrderDO order1 = new OrderDO();
        order1.setProductId("p-9301");
        order1.setProductName("Book A");
        order1.setQuantity(1);
        order1.setTotalAmount(new BigDecimal("100.00"));

        OrderDO order2 = new OrderDO();
        order2.setProductId("p-9302");
        order2.setProductName("Book B");
        order2.setQuantity(2);
        order2.setTotalAmount(new BigDecimal("200.00"));

        OrderDO order3 = new OrderDO();
        order3.setProductId("p-9303");
        order3.setProductName("Book C");
        order3.setQuantity(3);
        order3.setTotalAmount(new BigDecimal("300.00"));

        orderMapper.insert(order1);
        orderMapper.insert(order2);
        orderMapper.insert(order3);

        List<OrderDO> orders = orderMapper.selectBatchIds(List.of("p-9301", "p-9303"));

        assertThat(orders)
                .extracting(OrderDO::getProductId)
                .containsExactlyInAnyOrder("p-9301", "p-9303");
    }

    @Test
    void shouldDeleteOrdersByBatchIds() {
        OrderDO order1 = new OrderDO();
        order1.setProductId("p-9401");
        order1.setProductName("Book A");
        order1.setQuantity(1);
        order1.setTotalAmount(new BigDecimal("100.00"));

        OrderDO order2 = new OrderDO();
        order2.setProductId("p-9402");
        order2.setProductName("Book B");
        order2.setQuantity(2);
        order2.setTotalAmount(new BigDecimal("200.00"));

        OrderDO order3 = new OrderDO();
        order3.setProductId("p-9403");
        order3.setProductName("Book C");
        order3.setQuantity(3);
        order3.setTotalAmount(new BigDecimal("300.00"));

        orderMapper.insert(order1);
        orderMapper.insert(order2);
        orderMapper.insert(order3);

        int affectedRows = orderMapper.deleteBatchIds(List.of("p-9401", "p-9403"));

        assertThat(affectedRows).isEqualTo(2);
        assertThat(orderMapper.selectById("p-9401")).isNull();
        assertThat(orderMapper.selectById("p-9402")).isNotNull();
        assertThat(orderMapper.selectById("p-9403")).isNull();
    }
}
