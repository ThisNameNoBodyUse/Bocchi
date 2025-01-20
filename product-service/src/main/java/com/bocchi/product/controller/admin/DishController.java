package com.bocchi.product.controller.admin;

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

import static com.bocchi.constant.MapConstant.RECORD;
import static com.bocchi.constant.MapConstant.TOTAL;
import static com.bocchi.constant.MessageConstant.*;


/**
 * 菜品管理
 */
@Slf4j
@Api(tags = "管理端菜品接口")
@RestController("adminDishController")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping("/insert")
    @ApiOperation("新增菜品")
    public Result<Object> insertDish(@RequestBody DishDto dishDto) {
        //自己构造保存方法,同时保存到dish表和dish_flavor表
        dishService.saveDishAndFlavor(dishDto);
        Long categoryId = dishDto.getCategoryId();
        //清除关于该分类的所有信息
        String keys = "dish_" + categoryId + "_1";
        redisTemplate.delete(keys);
        return Result.success(SAVE_DISH_SUCCESS);
    }

    /**
     * 编辑菜品
     * @param dishDto
     * @return
     */
    @PostMapping("/update")
    @ApiOperation("修改菜品")
    public Result<Object> saveDish(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        Long categoryId = dishDto.getCategoryId();
        log.info("categoryId: {}", categoryId);
        //自己构造更新方法,同时在dish表和dish_flavor表更新
        dishService.updateDishAndFlavor(dishDto);
        //清空关于所有菜品的redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success(UPDATE_DISH_SUCCESS);

    }

    /**
     * 菜品分页查询
     * @param page
     * @param size
     * @param search
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/info")
    public Result<Object> infoDish(@Param("page") Integer page, @Param("size") Integer size, @Param("search") String search) {
        log.info("page:{},size:{}", page, size);

        Page<Dish> dishPage = new Page<>(page, size);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        if (search != null && !search.isEmpty()) {
            queryWrapper.like(Dish::getName, search);
        }
        //获取菜品表分页查询结果
        dishService.page(dishPage, queryWrapper);

        Page<DishDto> dishDtoPage = new Page<>();
        //对象拷贝,除了"records"字段都拷贝(DishDto继承了Dish)
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        List<Dish> dishList = dishPage.getRecords();
        List<DishDto> dishDtoList = dishList.stream().map((item) -> {
            DishDto dto = new DishDto();

            BeanUtils.copyProperties(item, dto);
            Long categoryId = item.getCategoryId(); //获取categoryId,根据分类categoryId获取categoryName
            //根据分类id查询分类对象
            String categoryName = categoryService.getById(categoryId).getName();
            dto.setCategoryName(categoryName);
            return dto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);
        Map<String, Object> map = new HashMap<>();
        map.put(RECORD, dishDtoPage.getRecords());
        map.put(TOTAL, dishDtoPage.getTotal());
        return Result.success(map);

    }

    /**
     * 员工根据菜品id查询菜品信息
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

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @ApiOperation("批量删除菜品")
    @PostMapping("/batchDelete")
    public Result<Object> batchDeleteDish(@RequestBody List<Long> ids) {
        //先判断有没有菜品起售中,有的话不给删除
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, 1);
        long count = dishService.count(dishLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException(EXIT_DISH_ON_SALE);
        }
        //批量删除dish 表中的记录
        dishService.removeByIds(ids);
        //批量删除 dish_flavor 表中的记录
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);

        return Result.success(BATCH_DELETE_SUCCESS);
    }

    /**
     * 批量起售
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量起售")
    @PostMapping("/batchEnable")
    public Result<Object> batchEnableDish(@RequestBody List<Long> ids) {


        // 使用 LambdaUpdateWrapper 来批量更新菜品的状态
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        //in适用于多个值
        //eq表示等值
        updateWrapper.set(Dish::getStatus, 1).in(Dish::getId, ids);

        // 执行批量更新操作
        dishService.update(new Dish(), updateWrapper);

        //清空关于所有菜品的redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success(BATCH_ON_SALE_SUCCESS);
    }

    /**
     * 批量停售
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量停售")
    @PostMapping("/batchDisable")
    public Result<Object> batchDisableDish(@RequestBody List<Long> ids) {
        LambdaUpdateWrapper<Dish> deleteWrapper = new LambdaUpdateWrapper<>();
        deleteWrapper.set(Dish::getStatus, 0).in(Dish::getId, ids);
        dishService.update(new Dish(), deleteWrapper);

        //清空关于所有菜品的redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success(BATCH_STOP_SALE_SUCCESS);

    }


    /**
     * 根据指定菜品id删除对应菜品记录
     * 删除不需要清空redis,因为删除之前会先停售
     * @param id
     * @return
     */
    @ApiOperation("根据菜品id删除菜品")
    @DeleteMapping("/delete/{id}")
    public Result<Object> deleteDish(@PathVariable Long id) {
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getId, id).eq(Dish::getStatus, 1);
        long count = dishService.count(dishQueryWrapper);
        if (count > 0) {
            throw new CustomException(DISH_ON_SALE);
        }

        //删除dish表中字段
        dishService.removeById(id);
        //删除dish_flavor表中字段
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        dishFlavorService.remove(queryWrapper);

        return Result.success(DISH_DELETE_SUCCESS);
    }

    /**
     * 根据指定菜品id来起售
     * @param id
     * @return
     */
    @ApiOperation("根据菜品id起售菜品")
    @PostMapping("/enable/{id}")
    public Result<Object> EnableDishById(@PathVariable Long id) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus, 1).eq(Dish::getId, id);
        dishService.update(new Dish(), updateWrapper);

        //清空关于所有菜品的redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success(ON_SALE_SUCCESS);
    }

    /**
     * 根据菜品id停售菜品
     * @param id
     * @return
     */
    @ApiOperation("根据菜品id停售菜品")
    @PostMapping("/disable/{id}")
    public Result<Object> DisableDishById(@PathVariable Long id) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus, 0).eq(Dish::getId, id);
        dishService.update(new Dish(), updateWrapper);


        //清空关于所有菜品的redis缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success(STOP_SALE_SUCCESS);
    }


    @GetMapping("/category/{categoryId}")
    public Result<Object> getDishByCategoryId(@PathVariable Long categoryId) {
        log.info(String.valueOf(categoryId));
        //根据categoryId查找该种类的菜品(起售)
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, categoryId).orderByDesc(Dish::getSort).eq(Dish::getStatus, 1);
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);


        return Result.success(dishList);
    }


}
