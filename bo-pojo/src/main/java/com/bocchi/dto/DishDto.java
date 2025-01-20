package com.bocchi.dto;


import com.bocchi.entity.Dish;
import com.bocchi.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装页面提交的数据
 * DTO用于展示层和服务层之间的数据传输
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();


    //以下两个暂时用不到,,以后再看
    private String categoryName; //分类名称

    private Integer copies;

}
