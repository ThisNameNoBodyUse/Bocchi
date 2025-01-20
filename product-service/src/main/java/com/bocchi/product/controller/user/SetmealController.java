package com.bocchi.product.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocchi.dto.SetmealDto;
import com.bocchi.entity.Setmeal;
import com.bocchi.entity.SetmealDish;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.SetmealDishService;
import com.bocchi.product.service.SetmealService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@Slf4j
@Api(tags = "用户端套餐接口")
@RequestMapping("/user/setMeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 根据套餐id查询套餐
     * @param setmealId
     * @return
     */
    @ApiOperation("根据套餐id查询套餐信息")
    @GetMapping("/{setmealId}")
    public Result<Object> selectSetmeal(@PathVariable Long setmealId) {
        log.info("setmealId:{}", setmealId);

        //先根据setmealId获取到setmeal的对象
        Setmeal setmeal = setmealService.getById(setmealId);

        //获取categoryId,用于查询分类名称
        Long categoryId = setmeal.getCategoryId();

        String categoryName = categoryService.getById(categoryId).getName();

        //再根据setmealId获取到所有setmeal_dish对象
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealId);

        List<SetmealDish> dishList = setmealDishService.list(dishLambdaQueryWrapper);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setDishes(dishList);
        setmealDto.setCategoryId(categoryId);
        setmealDto.setCategoryName(categoryName);

        log.info("setmealDto:{}", setmealDto);

        return Result.success(setmealDto);
    }




}
