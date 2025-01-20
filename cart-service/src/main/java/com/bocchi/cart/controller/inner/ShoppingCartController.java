package com.bocchi.cart.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocchi.cart.service.ShoppingCartService;
import com.bocchi.entity.ShoppingCart;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("innerShoppingCart")
@Api(tags = "内部购物车接口")
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 根据用户id查询购物车列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    List<ShoppingCart> getCartsById(@PathVariable("userId") Long userId) {
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return shoppingCarts;
    }

    /**
     * 清空用户购物车数据
     *
     * @param userId
     */
    @DeleteMapping("/{userId}")
    void removeCartsById(@PathVariable("userId") Long userId) {
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    /**
     * 批量插入购物车
     * @param carts
     */
    @PostMapping
    public void CartInsertBatch(@RequestBody List<ShoppingCart> carts) {
        shoppingCartService.saveBatch(carts);
    }


}
