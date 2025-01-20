package com.bocchi.order.controller.inner;


import com.bocchi.entity.Orders;
import com.bocchi.order.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController("innerOrderController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    /**
     * 根据orderId获取订单基本信息
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public Orders getOrderByOrderId(@PathVariable("orderId") Long orderId) {
        Orders orders = ordersService.getById(orderId);
        return orders;
    }

    /**
     * 根据订orderId修改订单状态为待派送
     * @param orderId
     */
    @PostMapping("/{orderId}")
    public void updateOrderById(@PathVariable("orderId") Long orderId) {
        Orders orders = ordersService.getById(orderId);
        orders.setStatus(2);
        ordersService.updateById(orders);
    }



}
