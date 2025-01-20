package com.bocchi.cart.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
