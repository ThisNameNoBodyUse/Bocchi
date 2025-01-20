package com.bocchi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    private Long id;
    private String name;
    private String image;
    private Long orderId;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
    private Integer number;
    private BigDecimal amount;

}
