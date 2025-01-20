package com.bocchi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private Long id;
    private String number;
    private Integer status;
    private Long userId;
    private Long addressBookId; // 地址id
    private LocalDateTime orderTime; // 下单时间
    private LocalDateTime checkoutTime; // 结账时间
    private Integer payMethod; // 支付方式 1-vx 2-zfb
    private BigDecimal amount; // 实收金额
    private String remark; // 备注
    private String email;
    private String address;
    private String userName;
    private String consignee;
}
