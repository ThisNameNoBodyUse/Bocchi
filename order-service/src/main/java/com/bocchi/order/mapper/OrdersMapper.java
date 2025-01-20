package com.bocchi.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.Orders;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
