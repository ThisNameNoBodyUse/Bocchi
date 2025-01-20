package com.bocchi.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bocchi.entity.Orders;



public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);

    void repeatOrder(Long id);


}
