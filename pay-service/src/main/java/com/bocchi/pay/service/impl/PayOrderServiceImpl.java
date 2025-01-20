package com.bocchi.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.api.client.OrderClient;
import com.bocchi.context.BaseContext;
import com.bocchi.entity.Orders;
import com.bocchi.entity.PayOrder;
import com.bocchi.pay.mapper.PayOrderMapper;
import com.bocchi.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {

    private final OrderClient orderClient;

    /**
     * 根据订单id创建支付单
     * @param orderId
     */
    @Override
    public void createPayOrder(Long orderId) {
        Orders order = orderClient.getOrderByOrderId(orderId);
        if(order == null) return;
        Long userId = BaseContext.getCurrentUserId();
        PayOrder payOrder = new PayOrder();
        payOrder.setAmount(order.getAmount()); // 支付单金额
        payOrder.setBizUserId(userId);
        payOrder.setId(orderId);
        payOrder.setPayOrderNo(orderId);
        payOrder.setBizOrderNo(orderId);
        payOrder.setStatus(1); // 待支付
        this.save(payOrder);
    }
}
