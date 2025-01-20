package com.bocchi.product.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocchi.dto.DishDto;
import com.bocchi.dto.SetmealDto;
import com.bocchi.entity.Category;
import com.bocchi.entity.Dish;
import com.bocchi.entity.DishFlavor;
import com.bocchi.entity.Setmeal;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.DishFlavorService;
import com.bocchi.product.service.DishService;
import com.bocchi.product.service.SetmealService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("userCategoryController")
@Slf4j
@Api(tags = "用户端分类接口")
@RequestMapping("/user/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DISH_KEY_PREFIX = "dish_";
    private static final String SETMEAL_KEY_PREFIX = "setmeal_";
    private static final String CATEGORY_KEY = "category_list";
    private static final long CACHE_DURATION = 24;  // 缓存时间24h

    /**
     * 查询该分类之下的菜品(套餐)信息
     *
     * @param type
     * @param categoryId
     * @return
     */
    @GetMapping("/type={type}/{categoryId}")
    @ApiOperation("根据分类id查询菜品/套餐 信息")
    public Result<Object> type(@PathVariable Integer type, @PathVariable Long categoryId) {
        if (type == 1) {
            return getDishInfo(categoryId, type);
        } else {
            return getSetmealInfo(categoryId, type);
        }
    }

    /**
     * 分类列表
     *
     * @return
     */
    @ApiOperation("查找分类列表")
    @GetMapping("/list")
    public Result<Object> list() {
        List<Category> categoryList = getCachedData(CATEGORY_KEY);
        if (categoryList != null) {
            return Result.success(categoryList);
        }

        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<>();
        query.orderByAsc(Category::getSort);
        categoryList = categoryService.list(query);

        cacheData(CATEGORY_KEY, categoryList);
        return Result.success(categoryList);
    }

    private Result<Object> getDishInfo(Long categoryId, Integer type) {
        String key = DISH_KEY_PREFIX + categoryId + "_" + type;
        List<DishDto> dishDtoList = getCachedData(key);
        if (dishDtoList != null) {
            return Result.success(dishDtoList);
        }
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, categoryId)
                .orderByDesc(Dish::getSort)
                .eq(Dish::getStatus, 1);
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);
        dishDtoList = dishList.stream().map(item -> {
            DishDto dto = new DishDto();
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dto.setFlavors(flavors);
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        cacheData(key, dishDtoList);
        return Result.success(dishDtoList);
    }

    private Result<Object> getSetmealInfo(Long categoryId, Integer type) {
        String key = SETMEAL_KEY_PREFIX + categoryId + "_" + type;
        List<SetmealDto> setmealDtoList = getCachedData(key);
        if (setmealDtoList != null) {
            return Result.success(setmealDtoList);
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, categoryId)
                .orderByDesc(Setmeal::getUpdateTime)
                .eq(Setmeal::getStatus, 1);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        setmealDtoList = setmealList.stream().map(item -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            dto.setFlavors(new ArrayList<>());
            return dto;
        }).collect(Collectors.toList());
        cacheData(key, setmealDtoList);
        return Result.success(setmealDtoList);
    }

    /**
     * 获取缓存数据
     * @param key
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getCachedData(String key) {
        return (List<T>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存数据
     * CACHE_DURATION : 缓存时间的数值
     * TimeUnit. ： 缓存时间的单位
     * @param key
     * @param data
     */
    private void cacheData(String key, List<?> data) {
        redisTemplate.opsForValue().set(key, data, CACHE_DURATION, TimeUnit.HOURS);
    }
}
