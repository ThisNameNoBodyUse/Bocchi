package com.bocchi.product.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.bocchi.dto.SetmealDto;
import com.bocchi.entity.Setmeal;
import com.bocchi.entity.SetmealDish;
import com.bocchi.exception.CustomException;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.SetmealDishService;
import com.bocchi.product.service.SetmealService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bocchi.constant.MapConstant.RECORD;
import static com.bocchi.constant.MapConstant.TOTAL;
import static com.bocchi.constant.MessageConstant.*;

@RestController("adminSetmealController")
@Slf4j
@Api(tags = "管理端套餐接口")
@RequestMapping("/admin/setMeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 插入新套餐
     * 同时插入到setmeal表和setmeal_dish表
     *
     * @param setmealDto
     * @return
     */
    @ApiOperation("新增套餐")
    @PostMapping("/insert")
    public Result<Object> insertSetmeal(@RequestBody SetmealDto setmealDto) {
        Long categoryId = setmealDto.getCategoryId();
        setmealService.saveSetmealAndDish(setmealDto);

        //清除该分类的redis
        String key = "setmeal_" + categoryId + "_2";
        redisTemplate.delete(key);

        return Result.success();
    }

    /**
     * 套餐分页查询
     *
     * @return
     */
    @ApiOperation("套餐分页查询")
    @GetMapping("/info")
    public Result<Object> infoSetmeal(@Param("page") Integer page, @Param("size") Integer size, @Param("search") String search) {
        log.info("page:{},size:{},search:{}", page, size, search);

        Page<Setmeal> setmealPage = new Page<>(page, size);
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (search != null && !search.isEmpty()) {
            setmealLambdaQueryWrapper.like(Setmeal::getName, search);
        }

        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(setmealPage, setmealLambdaQueryWrapper);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<Setmeal> setmealList = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //从setmeal拷贝给当前dto对象
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            //根据id查询分类名称
            String categoryName = categoryService.getById(categoryId).getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        Map<String, Object> result = new HashMap<>();
        result.put(TOTAL, setmealDtoPage.getTotal());
        result.put(RECORD, setmealDtoPage.getRecords());
        return Result.success(result);

    }

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

    /**
     * 编辑套餐
     * @param setmealDto
     * @return
     */
    @ApiOperation("修改套餐")
    @PostMapping("/update")
    public Result<Object> updateSetmeal(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto:{}", setmealDto);
        setmealService.updateSetmealAndDish(setmealDto);

        //清除所有套餐的redis
        Set keys = redisTemplate.keys("setmeal_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据指定套餐id删除对应套餐
     * 删除不需要清空redis,因为删除之前会先停售
     * @param id
     * @return
     */
    @ApiOperation("根据套餐id删除套餐")
    @DeleteMapping("/delete/{id}")
    public Result<Object> deleteDish(@PathVariable Long id) {
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        setmealLambdaQueryWrapper.eq(Setmeal::getId, id).eq(Setmeal::getStatus, 1);
        long count = setmealService.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException(SETMEAL_ON_SALE);
        }
        //删除setmeal表中字段
        setmealService.removeById(id);
        //删除setmeal_dish表中字段
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        setmealDishService.remove(dishLambdaQueryWrapper);


        return Result.success(SETMEAL_DELETE_SUCCESS);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @ApiOperation("批量删除套餐")
    @PostMapping("/batchDelete")
    public Result<Object> batchDeleteDish(@RequestBody List<Long> ids) {
        //先判断有没有菜品起售中,有的话不给删除
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);
        long count = setmealService.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException(EXIT_SETMEAL_ON_SALE);
        }
        //批量删除setmeal表中的记录
        setmealService.removeByIds(ids);
        //删除setmeal_dish表中记录
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishLambdaQueryWrapper);


        return Result.success(BATCH_DELETE_SUCCESS);
    }

    /**
     * 根据指定套餐id来起售
     *
     * @param id
     * @return
     */
    @ApiOperation("起售套餐")
    @PostMapping("/enable/{id}")
    public Result<Object> EnableDishById(@PathVariable Long id) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 1).eq(Setmeal::getId, id);
        setmealService.update(new Setmeal(), updateWrapper);

        //清除所有套餐的redis
        Set keys = redisTemplate.keys("setmeal_*");
        redisTemplate.delete(keys);

        return Result.success(ON_SALE_SUCCESS);
    }

    /**
     * 根据指定id停售套餐
     * @param id
     * @return
     */
    @ApiOperation("停售套餐")
    @PostMapping("/disable/{id}")
    public Result<Object> DisableDishById(@PathVariable Long id) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 0).eq(Setmeal::getId, id);
        setmealService.update(new Setmeal(), updateWrapper);

        //清除所有套餐的redis
        Set keys = redisTemplate.keys("setmeal_*");
        redisTemplate.delete(keys);

        return Result.success(STOP_SALE_SUCCESS);
    }

    /**
     * 批量停售
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量停售套餐")
    @PostMapping("/batchDisable")
    public Result<Object> batchDisableSetMeal(@RequestBody List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 0).in(Setmeal::getId, ids);
        setmealService.update(new Setmeal(), updateWrapper);

        //清除所有套餐的redis
        Set keys = redisTemplate.keys("setmeal_*");
        redisTemplate.delete(keys);


        return Result.success(BATCH_STOP_SALE_SUCCESS);

    }

    /**
     * 批量起售
     *
     * @param ids
     * @return
     */
    @ApiOperation("批量起售套餐")
    @PostMapping("/batchEnable")
    public Result<Object> batchEnableSetMeal(@RequestBody List<Long> ids) {


        // 使用 LambdaUpdateWrapper 来批量更新套餐的状态
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Setmeal::getStatus, 1).in(Setmeal::getId, ids);

        // 执行批量更新操作
        setmealService.update(new Setmeal(), updateWrapper);

        //清除所有套餐的redis
        Set keys = redisTemplate.keys("setmeal_*");
        redisTemplate.delete(keys);


        return Result.success(BATCH_ON_SALE_SUCCESS);
    }


}
