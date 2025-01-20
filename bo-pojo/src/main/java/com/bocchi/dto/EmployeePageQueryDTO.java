package com.bocchi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String search;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
