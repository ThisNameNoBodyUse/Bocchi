package com.bocchi.pay.controller.user;

import com.bocchi.api.client.OrderClient;
import com.bocchi.entity.PayOrder;
import com.bocchi.pay.service.PayOrderService;
import com.bocchi.result.Result;
import com.bocchi.utils.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.bocchi.constant.MessageConstant.*;

@RestController("userPayController")
@RequestMapping("/user/pay")
@Api(tags = "用户支付服务接口")
@RequiredArgsConstructor
@Slf4j
public class PayController {
    private final PayOrderService payOrderService;

    private final OrderClient orderClient;

    private final RedisTemplate redisTemplate;

    private static final String LOCK_KEY_PREFIX = "PAY_LOCK_";

    /**
     * 根据订单id创建支付单，初始状态为待支付
     *
     * @param orderId
     * @return
     */
    @ApiOperation("创建支付单")
    @PostMapping("/{orderId}")
    public Result<Object> createPayOrder(@PathVariable Long orderId) {
        payOrderService.createPayOrder(orderId);
        return Result.success();
    }

    /**
     * 支付处理
     * 根据订单id/支付单id 修改支付单状态为已支付
     * 前端防抖，后端幂等性检验
     * 同时后端实现分布式锁，确保同一时间只有一个实例能处理该支付请求，从而避免重复支付
     *
     * @param orderId
     * @return
     */
    @ApiOperation("支付操作,修改支付单状态为已支付")
    @PostMapping("/update/{orderId}")
    public Result<Object> updatePayOrderStatus(@PathVariable Long orderId) {
        String lockKey = LOCK_KEY_PREFIX + orderId;
        String requestId = String.valueOf(Thread.currentThread().getId());

        // 尝试获取锁，设置过期时间为30秒
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, 30, TimeUnit.SECONDS);

        // 引入分布式锁 2024.10.28
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                PayOrder payOrder = payOrderService.getById(orderId);
                if (payOrder == null) {
                    return Result.error(PAY_ORDER_IS_NULL);
                }
                // 幂等性检验，防止重复支付
                if (payOrder.getStatus() == 3) {
                    return Result.error(HAD_PAYED);
                }
                // TODO 扣款逻辑 2024.10.26
                // 扣款，没有设置支付，只能直接当做支付成功
                // 这里是个漏洞，由于没有营业执照设置真正的扣款
                // 所以用户可以通过postman等其他工具直接网络请求发起支付
                // 虽然没有扣款，但是仍可以验证是否超时来拒绝部分请求

                // 检查订单是否超时
                LocalDateTime createTime = payOrder.getCreateTime();
                LocalDateTime nowTime = TimeUtil.getNowTime();
                Duration duration = Duration.between(createTime, nowTime);
                if (duration.getSeconds() > 30) {
                    return Result.error(ORDER_TIME_OUT);
                }

                payOrder.setStatus(3); // 已支付
                payOrder.setPaySuccessTime(TimeUtil.getNowTime().withNano(0));
                payOrderService.updateById(payOrder);

                // 修改订单状态已支付
                orderClient.updateOrderById(orderId);

                return Result.success();
            } finally {
                // 释放锁 只有锁的value = ARGV[1] ,才能释放 否则可能没有获得锁的线程释放了锁 确保原子性
                // 防止误删锁
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 传入三参数 ： 构造器 KEYS[1]锁的键名 ARGV[1]锁的值
                redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList(lockKey), requestId);
            }
        } else {
            return Result.error(GET_PAY_LOCK_FAILED);
        }
    }
    @ApiOperation("获取支付单状态")
    @GetMapping("/{orderId}")
    public Result<Object> getPayOrderStatus(@PathVariable Long orderId) {
        PayOrder payOrder = payOrderService.getById(orderId);
        if (payOrder == null) {
            return Result.error(PAY_ORDER_IS_NULL);
        }
        return Result.success(payOrder.getStatus()); // 返回支付单状态
    }
}
