package com.bocchi.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressBook {
    private Long id;
    private Long userId;
    private String consignee; //收货人
    private Integer sex; //0女 1男
    private String email;
    private String provinceCode; //省级区域编号
    private String provinceName; //省级名称
    private String cityCode; //市级区域编号
    private String cityName; //市级名称
    private String districtCode; //区级区域编号
    private String districtName; //区级名称
    private String detail; //详细地址
    private String label; //标签
    private Integer isDefault; //默认 : 0否 1是

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    private Integer isDeleted; //是否删除

}
