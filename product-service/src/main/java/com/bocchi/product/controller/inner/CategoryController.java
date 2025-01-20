package com.bocchi.product.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocchi.entity.Category;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.DishFlavorService;
import com.bocchi.product.service.DishService;
import com.bocchi.product.service.SetmealService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("innerCategoryController")
@Api(tags = "分类相关接口")
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    @Autowired
    DishFlavorService dishFlavorService;

    @PostMapping("/list")
    public Result<Object> dish(@RequestBody Category category) {
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getType, category.getType());
        List<Category> categoryList = categoryService.list(categoryLambdaQueryWrapper);
        return Result.success(categoryList);
    }

}
