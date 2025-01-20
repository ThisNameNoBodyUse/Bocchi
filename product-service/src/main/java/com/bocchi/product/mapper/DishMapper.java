package com.bocchi.product.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
