package com.bocchi.product.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.bocchi.entity.Category;
import com.bocchi.product.service.CategoryService;
import com.bocchi.product.service.DishFlavorService;
import com.bocchi.product.service.DishService;
import com.bocchi.product.service.SetmealService;
import com.bocchi.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bocchi.constant.MapConstant.RECORD;
import static com.bocchi.constant.MapConstant.TOTAL;
import static com.bocchi.constant.MessageConstant.INSERT_FAILED;

@RestController("adminCategoryController")
@Api(tags = "分类相关接口")
@Slf4j
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    @Autowired
    DishFlavorService dishFlavorService;

    @Autowired
    RedisTemplate redisTemplate;

    private static final String CATEGORY_KEY = "category_list";


    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @ApiOperation("分类分页查询")
    @GetMapping("/info")
    public Result<Object> info(@RequestParam int page, @RequestParam int size) {

        //分页构造器
        Page<Category> pageRequest = new Page<>(page, size);
        //条件构造器
        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<>();
        //添加排序条件
        query.orderByAsc(Category::getSort);
        //执行分页查询
        Page<Category> categoryPage = categoryService.page(pageRequest, query);
        Map<String, Object> map = new HashMap<>();
        //返回结果,包含总数和当页数据
        map.put(TOTAL, categoryPage.getTotal());
        map.put(RECORD, categoryPage.getRecords());

        return Result.success(map);

    }


    /**
     * 新增分类
     *
     * @param category
     * @return
     */
    @ApiOperation("新增分类")
    @PostMapping("/insert")
    public Result<Object> insert(@RequestBody Category category) {

        boolean result = categoryService.save(category);

        if (result) {
            clearCategoryData();
            return Result.success();
        } else {
            return Result.error(INSERT_FAILED);
        }

    }

    /**
     * 根据分类id删除分类
     *
     * @param id
     * @return
     */
    @ApiOperation("根据分类id删除分类")
    @DeleteMapping
    public Result<Object> delete(Long id) {
        //localhost:8080/category?id=.......

        //要检查该分类是否关联了菜品或者套餐,如果关联了不给删除

        //使用自定义的remove方法
        categoryService.remove(id);
        clearCategoryData(); // 清空分类缓存
        deleteItemData(id); // 删除指定分类下的信息

        return Result.success();
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @ApiOperation("修改分类信息")
    @PostMapping("/update")
    public Result<Object> update(@RequestBody Category category) {
        log.info("修改分类信息: {}", category);
        categoryService.updateById(category);
        clearCategoryData();
        return Result.success();
    }

    /**
     * 查看分类列表
     *
     * @param category
     * @return
     */
    @ApiOperation("查看分类列表")
    @PostMapping("/list")
    public Result<Object> dish(@RequestBody Category category) {
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getType, category.getType());
        List<Category> categoryList = categoryService.list(categoryLambdaQueryWrapper);
        return Result.success(categoryList);
    }

    /**
     * 清空分类缓存数据
     */
    private void clearCategoryData() {
        redisTemplate.delete(CATEGORY_KEY);
    }

    /**
     * 删除该分类下的商品信息
     * 由于不知道该分类是菜品分类还是套餐分类
     * 所以都尝试删除
     *
     * @param categoryId
     */
    private void deleteItemData(Long categoryId) {
        redisTemplate.delete("dish_" + categoryId + "_1");
        redisTemplate.delete("setmeal_" + categoryId + "_2");
    }

}
