package com.bocchi.pay.controller.inner;

import com.bocchi.context.BaseContext;
import com.bocchi.entity.PayOrder;
import com.bocchi.pay.service.PayOrderService;
import com.bocchi.utils.TimeUtil;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController("innerPayController")
@RequestMapping("/pay")
@Api(tags = "内部支付服务接口")
@RequiredArgsConstructor
@Slf4j
public class PayController {
    private final PayOrderService payOrderService;

    /**
     * 根据id查询支付单
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public PayOrder getPayOrderById(@PathVariable("orderId") Long orderId) {
        PayOrder payOrder = payOrderService.getById(orderId);
        return payOrder;
    }

    /**
     * 修改支付单状态为支付超时
     * @param orderId
     */
    @PostMapping("/{orderId}")
    public void updatePayOrderById(@PathVariable("orderId") Long orderId) {
        PayOrder payOrder = payOrderService.getById(orderId);
        payOrder.setPayOverTime(TimeUtil.getNowTime().withNano(0));
        payOrder.setStatus(2); // 支付超时
        payOrder.setUpdateTime(TimeUtil.getNowTime().withNano(0));
        payOrderService.updateById(payOrder);
    }
}
