package com.bocchi.manager.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocchi.entity.Employee;
import com.bocchi.manager.mapper.EmployeeMapper;
import com.bocchi.manager.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service //标记在Service实现类上
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
