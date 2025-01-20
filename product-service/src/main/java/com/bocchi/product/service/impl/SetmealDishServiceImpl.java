package com.bocchi.product.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.SetmealDish;
import com.bocchi.product.mapper.SetmealDishMapper;
import com.bocchi.product.service.SetmealDishService;
import org.springframework.stereotype.Service;

@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
