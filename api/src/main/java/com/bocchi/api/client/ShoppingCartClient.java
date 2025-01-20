package com.bocchi.api.client;

import com.bocchi.entity.ShoppingCart;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("cart-service")
public interface ShoppingCartClient {

    @GetMapping("/shoppingCart/{userId}")
    List<ShoppingCart> getCartsById(@PathVariable("userId") Long userId);

    @DeleteMapping("/shoppingCart/{userId}")
    void removeCartsById(@PathVariable("userId") Long userId);

    @PostMapping("/shoppingCart")
    void CartInsertBatch(@RequestBody List<ShoppingCart> carts);


}
