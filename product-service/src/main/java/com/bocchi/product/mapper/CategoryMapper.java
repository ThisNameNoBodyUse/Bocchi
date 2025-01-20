package com.bocchi.product.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
