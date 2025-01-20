package com.bocchi.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocchi.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
