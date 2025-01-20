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
public class SetmealDish {
    private Long id;
    private Long setmealId; //套餐id
    private Long dishId; //菜品id
    private Long categoryId; //菜品分类id
    private String name; //菜品名(冗余字段)
    private BigDecimal price; //菜品价格
    private Integer copies; //菜品份数
    private Integer sort; //排序

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    private Integer isDeleted;
}
