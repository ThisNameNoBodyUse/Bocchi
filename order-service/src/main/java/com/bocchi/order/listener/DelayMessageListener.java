package com.bocchi.order.listener;

import com.bocchi.api.client.PayClient;
import com.bocchi.constant.MQConstant;
import com.bocchi.context.BaseContext;
import com.bocchi.entity.Orders;
import com.bocchi.entity.PayOrder;
import com.bocchi.order.service.impl.OrdersServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DelayMessageListener {

    private final PayClient payClient;

    private final OrdersServiceImpl ordersService;

    /**
     * TODO 新增支付微服务 10.14
     * TODO 根据支付单状态 是否修改订单状态为已支付？ 10.14
     * 处理延迟队列消息
     * 在过期时，查询订单状态，修改订单状态
     * 交换机设置为支持延迟插件的交换机 delayed = "true"
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstant.DELAY_QUEUE_NAME),
            exchange = @Exchange(name = MQConstant.DELAY_EXCHANGE_NAME, delayed = "true"),
            key = MQConstant.DELAY_ROUTING_KEY
    ))
    public void listenOrderDelayMessage(Long orderId) {
        try {
            // 手动设置上下文，比如从订单中获取用户ID并设置到ThreadLocal中
            Orders orders = ordersService.getById(orderId);
            if (orders != null) {
                BaseContext.setCurrentUserId(orders.getUserId());
            }

            // 检查订单是否为空，避免空指针异常
            if (orders == null || orders.getStatus() != 1) {
                return;
            }

            // 获取支付单信息
            PayOrder payOrder = payClient.getPayOrderById(orderId);
            if (payOrder == null || payOrder.getStatus() != 3) {
                // 支付单为空或者未支付，则取消订单
                orders.setStatus(5);
                ordersService.updateById(orders);
                // 修改支付单状态，支付超时
                payClient.updatePayOrderById(orderId);
                return;
            }

            // 订单修改为待派送
            orders.setStatus(2);
            ordersService.updateById(orders);
        } finally {
            // 清理ThreadLocal，避免内存泄漏
            BaseContext.removeCurrentUserId();
        }
    }


}
