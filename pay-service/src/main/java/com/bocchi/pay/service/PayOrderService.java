package com.bocchi.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bocchi.entity.PayOrder;


public interface PayOrderService extends IService<PayOrder> {

    void createPayOrder(Long orderId);
}
