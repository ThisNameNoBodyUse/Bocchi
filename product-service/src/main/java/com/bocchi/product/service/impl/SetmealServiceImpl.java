package com.bocchi.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.dto.SetmealDto;
import com.bocchi.entity.Setmeal;
import com.bocchi.entity.SetmealDish;
import com.bocchi.product.mapper.SetmealMapper;
import com.bocchi.product.service.SetmealDishService;
import com.bocchi.product.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveSetmealAndDish(SetmealDto setmealDto) {
        //同时插入setmeal表和setmeal_dish表
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getDishes();
        setmealDishes = setmealDishes.stream().peek((item)-> item.setSetmealId(setmealId)).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void updateSetmealAndDish(SetmealDto setmealDto) {

        //先更新setmeal表
        this.updateById(setmealDto);


        LambdaQueryWrapper<SetmealDish>setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

        //获取List<SetmealDish>列表,遍历删除,插入setmealDish
        List<SetmealDish>setmealDishes = setmealDto.getDishes();
        Long setmealId = setmealDto.getId(); //套餐id
        log.info("setmealId:{}", setmealId);

        setmealDishes = setmealDishes.stream().peek((item)-> item.setSetmealId(setmealId)).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);

    }
}
