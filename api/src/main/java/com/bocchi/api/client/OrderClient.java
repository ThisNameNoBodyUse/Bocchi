package com.bocchi.api.client;

import com.bocchi.entity.Orders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("order-service")
public interface OrderClient {

    /**
     * 根据orderId获取订单基本信息
     * @param orderId
     * @return
     */
    @GetMapping("/orders/{orderId}")
    Orders getOrderByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("/orders/{orderId}")
    void updateOrderById(@PathVariable("orderId") Long orderId);

}
