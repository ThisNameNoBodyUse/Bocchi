package com.bocchi.product.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bocchi.entity.Category;

public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
