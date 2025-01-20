package com.bocchi.cart.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.cart.mapper.ShoppingCartMapper;
import com.bocchi.cart.service.ShoppingCartService;
import com.bocchi.entity.ShoppingCart;
import org.springframework.stereotype.Service;


@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
