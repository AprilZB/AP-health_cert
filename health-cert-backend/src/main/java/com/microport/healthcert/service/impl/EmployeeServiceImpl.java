package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.service.EmployeeService;
import com.microport.healthcert.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 员工管理服务实现类
 *
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 查询员工列表（支持分页和筛选）
     *
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件（如name, sfUserId, isActive）
     * @return 分页的员工列表
     */
    @Override
    public Page<Employee> getEmployeeList(Integer page, Integer size, Map<String, Object> filters) {
        Page<Employee> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();

        if (filters != null) {
            // 员工姓名（模糊查询）
            if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().isEmpty()) {
                wrapper.like(Employee::getName, filters.get("name").toString());
            }
            // 员工域账号（精确查询）
            if (filters.containsKey("sfUserId") && filters.get("sfUserId") != null && !filters.get("sfUserId").toString().isEmpty()) {
                wrapper.eq(Employee::getSfUserId, filters.get("sfUserId").toString());
            }
            // 状态（精确查询）
            if (filters.containsKey("isActive") && filters.get("isActive") != null) {
                wrapper.eq(Employee::getIsActive, Integer.parseInt(filters.get("isActive").toString()));
            }
        }
        
        // 按ID降序排列（最新的在前）
        wrapper.orderByDesc(Employee::getId);
        
        return employeeMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 根据ID查询员工详情
     *
     * @param id 员工ID
     * @return 员工实体
     */
    @Override
    public Employee getEmployeeById(Long id) {
        return employeeMapper.selectById(id);
    }

    /**
     * 更新员工信息
     * 只允许更新邮箱和手机号字段，其他字段从远程HR系统同步，不允许手动修改
     *
     * @param employee 员工实体（只包含要更新的字段）
     * @param adminId 操作管理员ID
     * @param adminName 操作管理员名称
     * @return true成功，false失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployee(Employee employee, Long adminId, String adminName) {
        Employee existingEmployee = employeeMapper.selectById(employee.getId());
        if (existingEmployee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 只更新允许的字段：邮箱和手机号
        // 使用LambdaUpdateWrapper只更新指定字段，避免覆盖其他字段
        LambdaQueryWrapper<Employee> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(Employee::getId, employee.getId());
        
        // 构建更新对象，只包含要更新的字段
        Employee updateEmployee = new Employee();
        updateEmployee.setId(employee.getId());
        updateEmployee.setUpdatedAt(LocalDateTime.now());
        
        // 如果提供了邮箱，则更新
        if (employee.getEmail() != null) {
            updateEmployee.setEmail(employee.getEmail());
        }
        
        // 如果提供了手机号，则更新
        if (employee.getMobile() != null) {
            updateEmployee.setMobile(employee.getMobile());
        }
        
        // 如果提供了钉钉号，则更新
        if (employee.getDingtalkUserid() != null) {
            updateEmployee.setDingtalkUserid(employee.getDingtalkUserid());
        }
        
        // 使用MyBatis-Plus的update方法，只更新非null字段
        int rows = employeeMapper.updateById(updateEmployee);
        
        if (rows > 0) {
            String logDesc = "更新员工ID: " + employee.getId() + ", 姓名: " + existingEmployee.getName();
            if (employee.getEmail() != null) {
                logDesc += ", 邮箱: " + employee.getEmail();
            }
            if (employee.getMobile() != null) {
                logDesc += ", 手机号: " + employee.getMobile();
            }
            if (employee.getDingtalkUserid() != null) {
                logDesc += ", 钉钉号: " + employee.getDingtalkUserid();
            }
            operationLogService.saveLog(adminId, adminName, "admin", "更新员工", logDesc);
            return true;
        }
        return false;
    }
}

