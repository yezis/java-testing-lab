package com.example.testinglab.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.testinglab.common.model.PageResult;
import com.example.testinglab.order.domain.Order;
import com.example.testinglab.order.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    @Override
    public List<Order> findByProductNameAndMinTotalAmount(String productName, BigDecimal minTotalAmount) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);
        queryWrapper.ge(OrderDO::getTotalAmount, minTotalAmount);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByProductNameOrderByTotalAmountDesc(String productName) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);
        queryWrapper.orderByDesc(OrderDO::getTotalAmount);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByProductNameLike(String keyword) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(OrderDO::getProductName, keyword);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByTotalAmountBetweenOrderByTotalAmountAsc(BigDecimal minTotalAmount, BigDecimal maxTotalAmount) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(OrderDO::getTotalAmount, minTotalAmount);
        queryWrapper.le(OrderDO::getTotalAmount, maxTotalAmount);
        queryWrapper.orderByAsc(OrderDO::getTotalAmount);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByProductName(String productName) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);

        return orderMapper.selectCount(queryWrapper);
    }

    @Override
    public PageResult<Order> findByProductNamePageOrderByTotalAmountAsc(String productName, long pageNumber, long pageSize) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);
        queryWrapper.orderByAsc(OrderDO::getTotalAmount);

        Page<OrderDO> page = orderMapper.selectPage(new Page<>(pageNumber, pageSize), queryWrapper);
        List<Order> records = page.getRecords()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    @Override
    public List<Order> search(String productName, BigDecimal minTotalAmount, BigDecimal maxTotalAmount) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(productName != null, OrderDO::getProductName, productName);
        queryWrapper.ge(minTotalAmount != null, OrderDO::getTotalAmount, minTotalAmount);
        queryWrapper.le(maxTotalAmount != null, OrderDO::getTotalAmount, maxTotalAmount);
        queryWrapper.orderByAsc(OrderDO::getTotalAmount);

        return orderMapper.selectList(queryWrapper)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int updateQuantityByProductName(String productName, int quantity) {
        LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderDO::getProductName, productName);
        updateWrapper.set(OrderDO::getQuantity, quantity);

        return orderMapper.update(updateWrapper);
    }

    @Override
    public int deleteByProductName(String productName) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getProductName, productName);

        return orderMapper.delete(queryWrapper);
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
