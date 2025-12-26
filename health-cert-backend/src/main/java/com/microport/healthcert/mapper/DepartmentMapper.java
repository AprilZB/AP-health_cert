package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口
 * 对应实体类: Department
 * 对应数据库表: departments
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}

