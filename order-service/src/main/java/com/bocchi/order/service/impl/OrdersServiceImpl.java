package com.bocchi.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.api.client.ShoppingCartClient;
import com.bocchi.api.client.UserClient;
import com.bocchi.constant.MQConstant;
import com.bocchi.constant.MessageConstant;
import com.bocchi.context.BaseContext;
import com.bocchi.dto.OrderDto;
import com.bocchi.entity.*;
import com.bocchi.exception.CustomException;
import com.bocchi.exception.OrderBusinessException;
import com.bocchi.order.mapper.OrdersMapper;
import com.bocchi.order.service.OrderDetailService;
import com.bocchi.order.service.OrdersService;
import com.bocchi.utils.TimeUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final OrdersMapper ordersMapper;

    private final ShoppingCartClient shoppingCartClient;

    private final UserClient userClient;

    private final OrderDetailService orderDetailService;

    private final RabbitTemplate rabbitTemplate;

    @GlobalTransactional
    @Override
    public void submit(Orders orders) {

        //获得当前用户id
        Long userId = BaseContext.getCurrentUserId();

        //查询当前用户购物车数据
        List<ShoppingCart> shoppingCarts = shoppingCartClient.getCartsById(userId);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new CustomException("购物车为空，无法下单！");
        }
        //查询用户数据
        User user = userClient.getUserById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = userClient.getAddressBookById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误！");
        }
        Long orderId = IdWorker.getId();  //生成订单id
        //验证购物车数据  ----> 传金额不安全
        AtomicInteger amount = new AtomicInteger(0);  //原子操作,保证线程安全

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            //获取要插入的订单详情表列表,同时计算出订单总金额
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            //原子操作计算金额
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //向订单表插入一条数据
        orders.setId(orderId);
        orders.setOrderTime(TimeUtil.getNowTime().withNano(0));
        orders.setCheckoutTime(TimeUtil.getNowTime().withNano(0));
        orders.setStatus(1);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setEmail(addressBook.getEmail());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName()) +
                (addressBook.getCityName() == null ? "" : addressBook.getCityName()) +
                (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) +
                (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);
        //向订单详情表插入多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartClient.removeCartsById(userId);

        // TODO 发送消息给延迟交换机 10.14完成
        rabbitTemplate.convertAndSend(
                MQConstant.DELAY_EXCHANGE_NAME,
                MQConstant.DELAY_ROUTING_KEY,
                orderId,
                message -> {
                    message.getMessageProperties().setDelay(30000); //30s
                    return message;
                });
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @GlobalTransactional
    public void repeatOrder(Long id) {
        //根据订单id获取订单详情，插入到当前用户的购物车
        Long userId = BaseContext.getCurrentUserId(); //当前用户
        //插入前先清空购物车
        shoppingCartClient.removeCartsById(id);
        Orders order = new Orders();
        order.setId(id);
        order = this.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
        List<ShoppingCart> shoppingCarts = orderDetailList.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(TimeUtil.getNowTime().withNano(0));
            return shoppingCart;
        }).collect(Collectors.toList());

        //批量插入到购物车
        shoppingCartClient.CartInsertBatch(shoppingCarts);
    }


}
