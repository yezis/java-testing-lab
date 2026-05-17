package com.example.testinglab.order.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0)
            FROM orders
            WHERE product_name = #{productName}
            """)
    BigDecimal sumTotalAmountByProductName(@Param("productName") String productName);
}
