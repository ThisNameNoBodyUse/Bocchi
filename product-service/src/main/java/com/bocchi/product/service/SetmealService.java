package com.bocchi.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bocchi.dto.SetmealDto;
import com.bocchi.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    void saveSetmealAndDish(SetmealDto setmealDto);

    void updateSetmealAndDish(SetmealDto setmealDto);

}
