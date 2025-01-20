package com.bocchi.product.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.DishFlavor;
import com.bocchi.product.mapper.DishFlavorMapper;
import com.bocchi.product.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
