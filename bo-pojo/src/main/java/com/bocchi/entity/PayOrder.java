package com.bocchi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayOrder {

    private Long id; // id
    private Long bizOrderNo; // 业务订单号
    private Long payOrderNo; // 支付单号
    private Long bizUserId; // 支付用户id
    private BigDecimal amount; // 支付金额,实际金额*100
    private Integer status; // 订单状态 0 待提交 1 待支付 2 支付超时或取消 3 支付成功
    private LocalDateTime paySuccessTime;
    private LocalDateTime payOverTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    private Long updateUser;
}
