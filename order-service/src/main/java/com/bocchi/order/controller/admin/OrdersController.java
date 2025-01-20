package com.bocchi.order.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.bocchi.api.client.UserClient;
import com.bocchi.dto.OrderDto;
import com.bocchi.entity.OrderDetail;
import com.bocchi.entity.Orders;
import com.bocchi.entity.User;
import com.bocchi.order.service.OrderDetailService;
import com.bocchi.order.service.OrdersService;
import com.bocchi.result.Result;
import com.bocchi.utils.EmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bocchi.constant.MapConstant.RECORD;
import static com.bocchi.constant.MapConstant.TOTAL;
import static com.bocchi.constant.MessageConstant.ORDER_NOT_FOUND;

@RestController("adminOrderController")
@Slf4j
@Api(tags = "管理端订单接口")
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    private final UserClient userClient;


    /**
     * 订单分页查询
     *
     * @param page
     * @param size
     * @param search
     * @param startDate
     * @param endDate
     * @return
     */
    @ApiOperation("订单分页查询")
    @GetMapping("/info")
    public Result<Object> getOrdersInfo(@RequestParam("page") Integer page, @RequestParam("size") Integer size,
                                        @RequestParam(value = "search", required = false) Long search,
                                        @RequestParam(value = "startDate", required = false) String startDate,
                                        @RequestParam(value = "endDate", required = false) String endDate) {
        Page<Orders> ordersPage = new Page<>(page, size);
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (search != null) {
            ordersLambdaQueryWrapper.like(Orders::getId, search);
        }
        if (startDate != null && !startDate.isEmpty()) {
            ordersLambdaQueryWrapper.ge(Orders::getOrderTime, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            ordersLambdaQueryWrapper.le(Orders::getOrderTime, endDate);
        }
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        Page<Orders> page1 = ordersService.page(ordersPage, ordersLambdaQueryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put(RECORD, page1.getRecords());
        map.put(TOTAL, page1.getTotal());
        return Result.success(map);
    }


    /**
     * 根据订单id查询订单信息
     *
     * @param ordersId
     * @return
     */
    @ApiOperation("根据订单id查询订单信息")
    @GetMapping("/management/{ordersId}")
    public Result<Orders> getOrdersManagement(@PathVariable Long ordersId) {
        //当前用户id
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getNumber, String.valueOf(ordersId));
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

    /**
     * 更新订单状态
     *
     * @param orders
     * @return
     */
    @ApiOperation("更新订单状态")
    @PostMapping("/updateStatus")
    public Result<Object> updateOrderStatus(@RequestBody Orders orders) {
        // 订单是否存在
        Orders existingOrder = ordersService.getById(orders.getId());
        if (existingOrder == null) {
            return Result.error(ORDER_NOT_FOUND);
        }
        // 更新订单状态
        LambdaUpdateWrapper<Orders> ordersLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        ordersLambdaUpdateWrapper.eq(Orders::getId, orders.getId());
        ordersLambdaUpdateWrapper.set(Orders::getStatus, orders.getStatus());
        ordersService.update(ordersLambdaUpdateWrapper);

        // 发送订单状态更新邮件
        boolean result = EmailUtils.sendOrderStatusEmail(existingOrder.getEmail(), orders.getStatus());
        if (!result) {
            // 邮件对收件人无效，发送给购买者
            User user = userClient.getUserById(existingOrder.getUserId());
            EmailUtils.sendOrderStatusEmailToUser(user.getEmail(), orders.getStatus(), existingOrder.getEmail());
        }
        return Result.success();
    }


}
