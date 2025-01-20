package com.bocchi.order.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.bocchi.context.BaseContext;
import com.bocchi.dto.OrderDto;
import com.bocchi.entity.OrderDetail;
import com.bocchi.entity.Orders;
import com.bocchi.order.service.OrderDetailService;
import com.bocchi.order.service.OrdersService;
import com.bocchi.result.Result;
import com.bocchi.utils.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bocchi.constant.MapConstant.CREATE_TIME;
import static com.bocchi.constant.MapConstant.NOW_TIME;
import static com.bocchi.constant.MessageConstant.NEWEST_NOT_FOUND;
import static com.bocchi.constant.MessageConstant.ORDER_NOT_FOUND;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/orders")
@Api(tags = "用户订单服务接口")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单
     *
     * @param orders
     * @return
     */
    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<Object> submitOrder(@RequestBody Orders orders) {
        ordersService.submit(orders);
        Long orderId = orders.getId();
        return Result.success(orderId);
    }

    /**
     * 点餐完查看订单,根据订单号查看订单所有信息
     *
     * @param ordersId
     * @return
     */
    @ApiOperation("根据订单id查看订单详情")
    @GetMapping("/{ordersId}")
    public Result<Orders> getOrders(@PathVariable Long ordersId) {
        //当前用户id
        Long userId = BaseContext.getCurrentUserId();
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId, userId).eq(Orders::getNumber, String.valueOf(ordersId));
        //该订单基本信息
        Orders orders = ordersService.getOne(ordersLambdaQueryWrapper);
        //该订单详细信息
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, ordersId);
        List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(orders, orderDto);
        orderDto.setDetails(orderDetails);
        return Result.success(orderDto);
    }

    @ApiOperation("获取最新订单")
    @GetMapping("/latestOrder")
    public Result<Orders> getLatestOrders() {
        //当前用户id
        Long userId = BaseContext.getCurrentUserId();
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId, userId);
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime).last("limit 1");
        // 获取最新订单
        Orders latestOrder = ordersService.getOne(ordersLambdaQueryWrapper);

        if (latestOrder == null) {
            return Result.error(NEWEST_NOT_FOUND);
        }

        // 获取订单详细信息
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, latestOrder.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);

        // 创建订单DTO
        OrderDto orderDto = new OrderDto();
        BeanUtils.copyProperties(latestOrder, orderDto);
        orderDto.setDetails(orderDetails);

        return Result.success(orderDto);

    }

    /**
     * 查看当前用户订单列表
     * 优化 ： 2024-10-25
     *
     * @return
     */
    @ApiOperation("查看订单列表")
    @GetMapping("/list")
    public Result<Object> getOrdersList() {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 当前用户
        Long userId = BaseContext.getCurrentUserId();

        // 查询当前用户所有订单
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId, userId).orderByDesc(Orders::getOrderTime);
        List<Orders> ordersList = ordersService.list(ordersLambdaQueryWrapper);

        // 获取所有订单ID
        List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return Result.success();
        }

        // 批量查询所有订单详情
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.in(OrderDetail::getOrderId, orderIds);
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);

        // 按订单ID分组订单详情
        Map<Long, List<OrderDetail>> orderDetailsMap = orderDetailList.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 将订单详情设置到OrderDto中
        List<OrderDto> orderDtos = ordersList.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item, orderDto);
            orderDto.setDetails(orderDetailsMap.getOrDefault(item.getId(), new ArrayList<>()));
            return orderDto;
        }).collect(Collectors.toList());

        // 记录结束时间
        long endTime = System.currentTimeMillis();

        // 打印查询耗时
        long duration = endTime - startTime;
        log.info("查询耗时: {} 毫秒", duration);

        return Result.success(orderDtos);
    }


    /**
     * 再来一单
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repeatOrder(@PathVariable Long id) {
        log.info("再来一单 : {}", id);
        ordersService.repeatOrder(id);
        return Result.success();
    }

    /**
     * 根据订单id查询订单创建时间，同时传递服务器当前时间
     *
     * @param orderId
     * @return
     */
    @GetMapping("/time/{orderId}")
    public Result<Object> getOrderTime(@PathVariable Long orderId) {
        Orders orders = ordersService.getById(orderId);
        if (orders == null) {
            return Result.error(ORDER_NOT_FOUND);
        }
        LocalDateTime createTime = orders.getOrderTime();
        LocalDateTime nowTime = TimeUtil.getNowTime();
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put(CREATE_TIME, createTime);
        map.put(NOW_TIME, nowTime);
        return Result.success(map);
    }


}
