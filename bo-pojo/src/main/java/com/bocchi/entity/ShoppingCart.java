package com.bocchi.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    private Long id;
    private String name; //购物项名称(可能是套餐/菜品)
    private String image; //购物项图片
    private Long userId; //用户id
    private Long dishId; //菜品id,如果是套餐,可为空
    private Long setmealId; //套餐id,如果是菜品,可为空
    private String dishFlavor; //菜品口味,如果是套餐为空,没有口味的菜品为空
    private Integer number; //购物项数
    private BigDecimal amount; //金额

    private LocalDateTime createTime;
}
