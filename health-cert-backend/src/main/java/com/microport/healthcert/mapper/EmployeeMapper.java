package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工信息Mapper接口
 * 对应实体类: Employee
 * 对应数据库表: employees
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}

