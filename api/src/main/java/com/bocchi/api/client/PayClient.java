package com.bocchi.api.client;

import com.bocchi.entity.PayOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("pay-service")
public interface PayClient {

    @GetMapping("/pay/{orderId}")
    PayOrder getPayOrderById(@PathVariable("orderId") Long orderId);

    @PostMapping("/pay/{orderId}")
    void updatePayOrderById(@PathVariable("orderId") Long orderId);
}
