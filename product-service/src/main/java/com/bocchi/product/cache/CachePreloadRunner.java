package com.bocchi.product.cache;


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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
public class CachePreloadRunner implements CommandLineRunner {

    private static final String CATEGORY_KEY = "category_list";
    private static final String DISH_KEY_PREFIX = "dish_";
    private static final String SETMEAL_KEY_PREFIX = "setmeal_";
    private static final long CACHE_DURATION = 24; // 缓存时间24小时

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 缓存预热
     *
     * @param args
     */
    @Override
    public void run(String... args) {
        log.info("开始预加载缓存数据...");
        preloadCacheData();
    }

    /**
     * 缓存定时刷新
     */
    @Scheduled(fixedRate = 86400000) // 每24小时执行一次
    public void refreshCache() {
        log.info("开始刷新缓存数据...");
        preloadCacheData();
        log.info("刷新缓存数据完成!");
    }

    /**
     * 预热数据获取
     */
    private void preloadCacheData() {
        // 预加载分类数据
        LambdaQueryWrapper<Category> categoryQuery = new LambdaQueryWrapper<>();
        categoryQuery.orderByAsc(Category::getSort);
        List<Category> categories = categoryService.list(categoryQuery);
        cacheData(CATEGORY_KEY, categories);

        // 根据分类预加载菜品和套餐数据
        for (Category category : categories) {
            Long categoryId = category.getId();
            Integer type = category.getType();

            if (type == 1) {
                // 预加载菜品数据
                String dishKey = DISH_KEY_PREFIX + categoryId + "_1";
                LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
                dishQuery.eq(Dish::getCategoryId, categoryId).eq(Dish::getStatus, 1).orderByDesc(Dish::getSort);
                List<Dish> dishList = dishService.list(dishQuery);
                List<DishDto> dishDtoList = dishList.stream().map(item -> {
                    DishDto dto = new DishDto();
                    Long dishId = item.getId();
                    LambdaQueryWrapper<DishFlavor> dishFlavorQuery = new LambdaQueryWrapper<>();
                    dishFlavorQuery.eq(DishFlavor::getDishId, dishId);
                    List<DishFlavor> flavors = dishFlavorService.list(dishFlavorQuery);
                    dto.setFlavors(flavors);
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                }).collect(Collectors.toList());
                cacheData(dishKey, dishDtoList);
            } else if (type == 2) {
                // 预加载套餐数据
                String setmealKey = SETMEAL_KEY_PREFIX + categoryId + "_2";
                LambdaQueryWrapper<Setmeal> setmealQuery = new LambdaQueryWrapper<>();
                setmealQuery.eq(Setmeal::getCategoryId, categoryId).eq(Setmeal::getStatus, 1).orderByDesc(Setmeal::getUpdateTime);
                List<Setmeal> setmealList = setmealService.list(setmealQuery);
                List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
                    SetmealDto dto = new SetmealDto();
                    BeanUtils.copyProperties(item, dto);
                    dto.setFlavors(new ArrayList<>());
                    return dto;
                }).collect(Collectors.toList());
                cacheData(setmealKey, setmealDtoList);
            }

        }
    }

    /**
     * 缓存数据
     *
     * @param key  键
     * @param data 数据
     */
    private void cacheData(String key, List<?> data) {
        redisTemplate.opsForValue().set(key, data, CACHE_DURATION, TimeUnit.HOURS);
    }
}

