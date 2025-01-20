package com.bocchi.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.dto.DishDto;
import com.bocchi.entity.Dish;
import com.bocchi.entity.DishFlavor;
import com.bocchi.product.mapper.DishMapper;
import com.bocchi.product.service.DishFlavorService;
import com.bocchi.product.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;


    @Override
    @Transactional
    public void saveDishAndFlavor(DishDto dishDto) {
        //先保存菜品
        this.save(dishDto); //dishDto保存自动生成Id为菜品id

        //再保存菜品口味
        Long dishId = dishDto.getId();
        List<DishFlavor>flavors = dishDto.getFlavors(); //口味链表
        //遍历口味链表保存到数据库中
        flavors = flavors.stream().peek((item)-> item.setDishId(dishId)).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表
        dishFlavorService.saveBatch(flavors); //saveBatch为关于集合的批量保存
    }

    @Override
    @Transactional
    public void updateDishAndFlavor(DishDto dishDto) {
        //先更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据--dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        Long dishId = dishDto.getId();
        //遍历口味表准备修改入数据库中
        flavors = flavors.stream().peek((item)-> item.setDishId(dishId)).collect(Collectors.toList());

        //批量保存
        dishFlavorService.saveBatch(flavors);
    }


}
