package com.microport.healthcert.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.common.Result;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理控制器
 * 提供员工查询、更新等接口
 *
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 查询员工列表（支持分页和筛选）
     *
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @param name 员工姓名（可选，模糊查询）
     * @param sfUserId 员工域账号（可选，精确查询）
     * @param isActive 状态（可选，精确查询 1在职 0离职）
     * @param request HttpServletRequest 用于获取管理员信息
     * @return 员工列表
     */
    @GetMapping("/list")
    public Result<Page<Employee>> getEmployeeList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sfUserId,
            @RequestParam(required = false) Integer isActive,
            HttpServletRequest request) {
        try {
            Map<String, Object> filters = new HashMap<>();
            if (name != null && !name.trim().isEmpty()) {
                filters.put("name", name);
            }
            if (sfUserId != null && !sfUserId.trim().isEmpty()) {
                filters.put("sfUserId", sfUserId);
            }
            if (isActive != null) {
                filters.put("isActive", isActive);
            }
            Page<Employee> result = employeeService.getEmployeeList(page, size, filters);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询员工列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询员工详情
     *
     * @param id 员工ID
     * @return 员工实体
     */
    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return Result.error(404, "员工不存在");
            }
            return Result.success(employee);
        } catch (Exception e) {
            return Result.error(500, "查询员工详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param employee 员工实体（包含要更新的字段）
     * @param request HttpServletRequest 用于获取管理员信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Object> updateEmployee(@PathVariable Long id, @RequestBody Employee employee, HttpServletRequest request) {
        try {
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");
            employee.setId(id); // 确保ID正确
            boolean success = employeeService.updateEmployee(employee, adminId, adminName);
            if (success) {
                return Result.success("员工信息更新成功");
            } else {
                return Result.error(500, "员工信息更新失败");
            }
        } catch (Exception e) {
            return Result.error(500, "更新员工信息失败：" + e.getMessage());
        }
    }
}

