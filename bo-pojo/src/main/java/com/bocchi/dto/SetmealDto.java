package com.bocchi.dto;


import com.bocchi.entity.Setmeal;
import com.bocchi.entity.SetmealDish;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDto extends Setmeal implements Serializable {
    private static final long serialVersionUID = 1L;


    private List<SetmealDish>dishes = new ArrayList<>();

    private String categoryName; //套餐名称

    private List<String>flavors = new ArrayList<>(); //冗余,并没有用
}
