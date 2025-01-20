package com.bocchi.manager.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocchi.auth.JwtService;
import com.bocchi.constant.HeaderConstant;
import com.bocchi.constant.JwtClaimsConstant;
import com.bocchi.context.BaseContext;
import com.bocchi.dto.EmployeePageQueryDTO;
import com.bocchi.entity.Employee;
import com.bocchi.manager.service.EmployeeService;
import com.bocchi.properties.JwtProperties;
import com.bocchi.result.Result;
import com.bocchi.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.bocchi.constant.MapConstant.RECORD;
import static com.bocchi.constant.MapConstant.TOTAL;
import static com.bocchi.constant.MessageConstant.*;

@RestController
@Slf4j
@RequestMapping("/admin/employee")
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtService jwtService;

    /**
     * 员工登录
     *
     * @param e
     * @return
     */
    @ApiOperation("员工登录")
    @PostMapping("/login")
    public Result<Object> login(@RequestBody Employee e) {
        log.info("employee login : {}", e);

        // 1. 将页面提交的密码password进行md5加密
        String password = e.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> query = new LambdaQueryWrapper<>();
        query.eq(Employee::getUsername, e.getUsername());
        Employee emp = employeeService.getOne(query);

        // 3. 没有查询到,则返回账号不存在
        if (emp == null) {
            return Result.error(ACCOUNT_NOT_FOUND);
        }

        // 4. 密码比对,不一致则返回密码错误
        if (!password.equals(emp.getPassword())) {
            return Result.error(PASSWORD_ERROR);
        }

        // 5. 查看员工状态,如果为已禁用,则返回账号被锁定
        if (emp.getStatus() == 0) {
            return Result.error(ACCOUNT_LOCKED);
        }

        // 6. 登录成功,将用户id放入jwt令牌发送给前端
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, emp.getId());

        String accessToken = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        String refreshToken = JwtUtil.createJWT(
                jwtProperties.getAdminRefreshSecretKey(),
                jwtProperties.getAdminRefreshTtl(),
                claims);

        Map<String, String> tokens = new HashMap<>();
        tokens.put(HeaderConstant.ADMIN_ACCESS, accessToken);
        tokens.put(HeaderConstant.ADMIN_REFRESH, refreshToken);

        return Result.success(tokens);
    }


    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @ApiOperation("员工退出登录")
    @PostMapping("/logout")
    public Result<Object> logout(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HeaderConstant.ADMIN_ACCESS);
            String refreshToken = request.getHeader(HeaderConstant.ADMIN_REFRESH);

            if ((accessToken == null || accessToken.isEmpty()) && (refreshToken == null || refreshToken.isEmpty())) {
                return Result.error(JWT_EMPTY);
            }

            // 使访问令牌失效
            if (accessToken != null && !accessToken.isEmpty()) {
                String accessJti = JwtUtil.getJti(jwtProperties.getAdminSecretKey(), accessToken);
                long accessExpireTime = JwtUtil.getExpireTime(jwtProperties.getAdminSecretKey(), accessToken);
                jwtService.storeToken(accessJti, accessExpireTime);
            }

            // 使刷新令牌失效
            if (refreshToken != null && !refreshToken.isEmpty()) {
                String refreshJti = JwtUtil.getJti(jwtProperties.getAdminRefreshSecretKey(), refreshToken);
                long refreshExpireTime = JwtUtil.getExpireTime(jwtProperties.getAdminRefreshSecretKey(), refreshToken);
                jwtService.storeToken(refreshJti, refreshExpireTime);
            }

            return Result.success(LOGOUT_SUCCESS);
        } catch (Exception e) {
            log.error("Parse JWT ERROR: ", e);
            // JWT无效
            return Result.error(JWT_INVALID);
        }
    }

    /**
     * @param employeePageQueryDTO
     * @return
     */
    @ApiOperation("员工分页查询")
    @GetMapping("/info")
    public Result<Object> info(EmployeePageQueryDTO employeePageQueryDTO) {
        /**
         * mybatis-plus分页查询自动执行两步:
         *     1. 求出除了limit外其他字段限制的记录数count(*),由getTotal()获取
         *     2. 求处当前页的数据(第page页,这一页最多size条记录),这些记录由getRecords()获取
         */
        // 创建分页对象
        int page = employeePageQueryDTO.getPage();
        int size = employeePageQueryDTO.getPageSize();
        String search = employeePageQueryDTO.getSearch();
        log.info("page:{},size:{},search:{}", page, size, search);
        Page<Employee> pageRequest = new Page<>(page, size);

        // 创建查询条件
        LambdaQueryWrapper<Employee> query = new LambdaQueryWrapper<>();
        if (search != null && !search.isEmpty()) {
            query.like(Employee::getName, search);
        }

        query.orderByDesc(Employee::getCreateTime);

        // 执行分页查询
        Page<Employee> employeePage = employeeService.page(pageRequest, query);

        // 返回结果,包含总条数和当前页的数据
        Map<String, Object> result = new HashMap<>();
        result.put(RECORD, employeePage.getRecords());
        result.put(TOTAL, employeePage.getTotal());
        return Result.success(result);
    }

    /**
     * 更新用户信息
     *
     * @param e
     * @return
     */
    @ApiOperation("更新员工信息")
    @PostMapping("/save")
    public Result<Object> save(@RequestBody Employee e) {

        // 判断当前员工是否是超级管理员
        Long id = BaseContext.getCurrentAdminId();
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.error(ACCOUNT_NOT_FOUND);
        }
        if (!employee.getUsername().equals(ADMIN_USERNAME)) {
            return Result.error(ACCOUNT_NOT_ADMIN);
        }
        // 如果要修改的是管理员,直接拒绝修改
        if(e.getUsername().equals(ADMIN_USERNAME)) {
            return Result.error(ADMIN_CANNOT_UPDATE_ADMIN);
        }

        boolean result = employeeService.updateById(e);

        if (result) {
            return Result.success();
        } else {
            return Result.error(SAVE_FAILED);
        }
    }


    /**
     * 删除员工
     *
     * @param id
     * @return
     */
    @ApiOperation("删除员工")
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id) {
        // 判断当前员工是否是超级管理员
        Long DoId = BaseContext.getCurrentAdminId();
        Employee employee = employeeService.getById(DoId);
        if (employee == null) {
            return Result.error(ACCOUNT_NOT_FOUND);
        }
        if (!employee.getUsername().equals(ADMIN_USERNAME)) {
            return Result.error(ACCOUNT_NOT_ADMIN);
        }
        // 查看要删除的员工是否是管理员
        Employee e = employeeService.getById(id);
        if (e.getUsername().equals(ADMIN_USERNAME)) {
            return Result.error(ADMIN_CANNOT_DELETE_ADMIN);
        }
        // 不是，准备删除
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getId, id);
        employeeService.remove(lqw);
        return Result.success();
    }

    /**
     * 添加员工
     */
    @ApiOperation("添加员工")
    @PostMapping("/insert")
    public Result<Object> insert(@RequestBody Employee e) {
        // 判断当前员工是否是超级管理员
        Long DoId = BaseContext.getCurrentAdminId();
        Employee employee = employeeService.getById(DoId);
        if (employee == null) {
            return Result.error(ACCOUNT_NOT_FOUND);
        }
        if (!employee.getUsername().equals(ADMIN_USERNAME)) {
            return Result.error(ACCOUNT_NOT_ADMIN);
        }

        e.setId(e.getId());
        e.setPassword(DigestUtils.md5DigestAsHex(INIT_PASSWORD.getBytes()));

        boolean result = employeeService.save(e);
        if (result) {
            return Result.success();
        } else {
            return Result.error(INSERT_FAILED);
        }
    }


}
