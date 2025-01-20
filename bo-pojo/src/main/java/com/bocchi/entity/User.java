package com.bocchi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;  //姓名
    private String email; //邮箱
    private String sex; //性别
    private String idNumber; //身份证号
    private String avatar;
    private Integer status; //0禁用 1正常
}
