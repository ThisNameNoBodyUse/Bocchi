package com.bocchi.product.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocchi.dto.DishDto;
import com.bocchi.entity.Dish;
import com.bocchi.entity.DishFlavor;
import com.bocchi.exception.CustomException;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.DishFlavorService;
import com.bocchi.product.service.DishService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 菜品管理
 */
@Slf4j
@RestController("userDishController")
@Api(tags = "用户端菜品接口")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;



    /**
     * 顾客根据菜品id查询菜品信息
     * @param id
     * @return
     */
    @ApiOperation("根据菜品id查询菜品信息")
    @GetMapping("/{id}")
    public Result<Object> getDishDetails(@PathVariable Long id) {

        //根据传进来的菜品id在dish表找到对应的对象
        Dish dish = dishService.getById(id);
        //根据传进来的菜品id找到口味列表
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        //获得该菜品的口味列表
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //找到该对象的categoryId
        Long categoryId = dish.getCategoryId();
        //获得该分类名称
        String categoryName = categoryService.getById(categoryId).getName();


        //封装成DishDto进行传输
        DishDto dto = new DishDto();
        BeanUtils.copyProperties(dish, dto);
        dto.setCategoryName(categoryName);
        dto.setFlavors(flavors);

        return Result.success(dto);

    }



}
