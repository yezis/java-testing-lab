package com.example.testinglab.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        orderMapper.insert(toDO(order));
        return order;
    }

    @Override
    public List<Order> findByProductName(String productName) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderDO toDO(Order order) {
        OrderDO orderDO = new OrderDO();
        orderDO.setProductId(order.getProductId());
        orderDO.setProductName(order.getProductName());
        orderDO.setQuantity(order.getQuantity());
        orderDO.setTotalAmount(order.getTotalAmount());
        return orderDO;
    }

    private Order toDomain(OrderDO orderDO) {
        return new Order(
                orderDO.getProductId(),
                orderDO.getProductName(),
                orderDO.getQuantity(),
                orderDO.getTotalAmount()
        );
    }
}
