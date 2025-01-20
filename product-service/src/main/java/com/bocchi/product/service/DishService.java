package com.bocchi.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bocchi.dto.DishDto;
import com.bocchi.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveDishAndFlavor(DishDto dishDto);

    void updateDishAndFlavor(DishDto dishDto);
}
