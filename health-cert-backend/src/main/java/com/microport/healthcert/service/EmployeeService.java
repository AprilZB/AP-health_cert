package com.microport.healthcert.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.Employee;

import java.util.Map;

/**
 * 员工管理服务接口
 * 提供员工查询、更新等功能
 *
 * @author system
 * @date 2024
 */
public interface EmployeeService {

    /**
     * 查询员工列表（支持分页和筛选）
     *
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件（如name, sfUserId, isActive）
     * @return 分页的员工列表
     */
    Page<Employee> getEmployeeList(Integer page, Integer size, Map<String, Object> filters);

    /**
     * 根据ID查询员工详情
     *
     * @param id 员工ID
     * @return 员工实体
     */
    Employee getEmployeeById(Long id);

    /**
     * 更新员工信息
     *
     * @param employee 员工实体
     * @param adminId 操作管理员ID
     * @param adminName 操作管理员名称
     * @return true成功，false失败
     */
    boolean updateEmployee(Employee employee, Long adminId, String adminName);
}

