package com.bocchi.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.Category;
import com.bocchi.entity.Dish;
import com.bocchi.entity.Setmeal;
import com.bocchi.exception.CustomException;
import com.bocchi.product.mapper.CategoryMapper;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.DishService;
import com.bocchi.product.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类
     * 删除之前需要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品,如果已经关联,抛出一个业务异常
        LambdaQueryWrapper<Dish>lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId, id);
        long dishCount = dishService.count(lambdaQueryWrapper);
        if (dishCount > 0) {
            //已经关联菜品,抛出一个业务异常
            throw new CustomException("当前分类关联了菜品,不能删除");
        }

        //查询当前分类是否关联了套餐,如果已经关联,抛出一个业务异常
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        long setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealCount > 0) {
            //已经关联套餐,抛出一个业务异常
            throw new CustomException("当前分类关联了套餐,不能删除");
        }

        //正常删除分类
        super.removeById(id);

    }
}
