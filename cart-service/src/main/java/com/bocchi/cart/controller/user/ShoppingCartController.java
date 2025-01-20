package com.bocchi.cart.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.bocchi.cart.service.ShoppingCartService;
import com.bocchi.constant.MessageConstant;
import com.bocchi.context.BaseContext;
import com.bocchi.entity.ShoppingCart;
import com.bocchi.result.Result;
import com.bocchi.utils.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController("userShoppingCart")
@Api(tags = "用户购物车接口")
@Slf4j
@RequestMapping("/user/shoppingCart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final RedisTemplate redisTemplate;

    @Autowired
    private ShoppingCartService shoppingCartService;

    private final String LOCK_PREFIX = "CART_LOCK_"; // 购物车锁

    /**
     * 增加购物项数量
     *
     * @param shoppingCart
     * @return
     */
    @ApiOperation("增加购物项")
    @PostMapping("/add")
    public Result<Object> add(@RequestBody ShoppingCart shoppingCart) {

        log.info("购物车数据 : {}", shoppingCart);

        //设置用户id
        Long currentId = BaseContext.getCurrentUserId();
        log.info("currentId: {}", currentId);

        // 引入分布式锁 2024 - 12 -2
        shoppingCart.setUserId(currentId);
        // 分布式锁
        String requestId = String.valueOf(Thread.currentThread().getId()); // 请求id
        String Lock_key = LOCK_PREFIX + currentId + ":" + (shoppingCart.getDishId() != null ? shoppingCart.getDishId() : shoppingCart.getSetmealId());
        Boolean canLock = redisTemplate.opsForValue().setIfAbsent(Lock_key, requestId, 2, TimeUnit.SECONDS); // 尝试获取分布式锁

        if (Boolean.FALSE.equals(canLock)) {
            return Result.error(MessageConstant.CART_FREQUENTLY);
        }
        try {
            //查询当前菜品是否在购物车中
            Long dishId = shoppingCart.getDishId();
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<ShoppingCart>();
            queryWrapper.eq(ShoppingCart::getUserId, currentId);
            if (dishId != null) {
                //添加入购物车的是菜品
                queryWrapper.eq(ShoppingCart::getDishId, dishId);
            } else {
                //添加入购物车的是套餐
                queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

            }
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
            if (shoppingCart1 != null) {
                //如果已经存在,则在原来数量基础上加一
                Integer number = shoppingCart1.getNumber();
                shoppingCart1.setNumber(number + 1);
                shoppingCartService.updateById(shoppingCart1);
            } else {
                //如果不存在,则添加到购物车,数量默认是1
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(TimeUtil.getNowTime().withNano(0));
                shoppingCartService.save(shoppingCart);
            }
        } finally {
            // 释放锁 只有锁的value = ARGV[1] ,才能释放 否则可能没有获得锁的线程释放了锁 确保原子性
            // 防止误删锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 传入三参数 ： 构造器 KEYS[1]锁的键名 ARGV[1]锁的值
            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList(Lock_key), requestId);
        }
        return Result.success(MessageConstant.CART_ADD_SUCCESS);
    }

    /**
     * 减少购物项数量
     *
     * @param shoppingCart
     * @return
     */
    @ApiOperation("减少购物项")
    @PostMapping("/sub")
    public Result<Object> sub(@RequestBody ShoppingCart shoppingCart) {
        Long currentId = BaseContext.getCurrentUserId();
        shoppingCart.setUserId(currentId);
        // 引入分布式锁(2024 - 12 - 2)

        // 分布式锁
        String requestId = String.valueOf(Thread.currentThread().getId()); // 请求id
        String Lock_key = LOCK_PREFIX + currentId + ":" + (shoppingCart.getDishId() != null ? shoppingCart.getDishId() : shoppingCart.getSetmealId());
        Boolean canLock = redisTemplate.opsForValue().setIfAbsent(Lock_key, requestId, 2, TimeUnit.SECONDS); // 尝试获取分布式锁
        if (Boolean.FALSE.equals(canLock)) {
            return Result.error(MessageConstant.CART_FREQUENTLY);
        }
        try {
            //查询当前菜品是否在购物车中
            Long dishId = shoppingCart.getDishId();
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getUserId, currentId);
            if (dishId != null) {
                //添加入购物车的是菜品
                queryWrapper.eq(ShoppingCart::getDishId, dishId);
            } else {
                //添加入购物车的是套餐
                queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
            }
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
            Integer number = shoppingCart1.getNumber();

            if (number > 1) {
                shoppingCart1.setNumber(number - 1);
                shoppingCartService.updateById(shoppingCart1);
            } else {
                shoppingCartService.removeById(shoppingCart1);
            }
        } finally {
            // 释放锁 只有锁的value = ARGV[1] ,才能释放 否则可能没有获得锁的线程释放了锁 确保原子性
            // 防止误删锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            // 传入三参数 ： 构造器 KEYS[1]锁的键名 ARGV[1]锁的值
            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList(Lock_key), requestId);
        }
        return Result.success(MessageConstant.CART_SUB_SUCCESS);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @ApiOperation("查看当前用户购物车")
    @GetMapping("/list")
    public Result<Object> list() {
        log.info("查看购物车");

        Long userId = BaseContext.getCurrentUserId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime); //最后加进来的菜品最先展示

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return Result.success(list);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @ApiOperation("清空当前用户购物车")
    @DeleteMapping("/clean")
    public Result<Object> clean() {
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentUserId());
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
        return Result.success(MessageConstant.CART_CLEAR_SUCCESS);
    }
}
