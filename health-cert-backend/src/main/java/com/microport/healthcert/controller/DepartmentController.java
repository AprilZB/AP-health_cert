package com.microport.healthcert.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.common.Result;
import com.microport.healthcert.entity.Department;
import com.microport.healthcert.service.DepartmentService;
import com.microport.healthcert.vo.DepartmentTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理控制器
 * 提供部门查询、更新等接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 查询部门列表（支持分页和筛选）
     * 
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @param deptName 部门名称（可选，模糊查询）
     * @param parentDeptName 上级部门名称（可选，精确查询）
     * @param isActive 是否启用（可选，1=启用，0=禁用）
     * @param deptLevel 部门层级（可选，精确查询）
     * @return 部门列表
     */
    @GetMapping("/list")
    public Result<Page<Department>> getDepartmentList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String parentDeptName,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(required = false) Integer deptLevel) {
        try {
            // 构建筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (deptName != null && !deptName.trim().isEmpty()) {
                filters.put("deptName", deptName);
            }
            if (parentDeptName != null && !parentDeptName.trim().isEmpty()) {
                filters.put("parentDeptName", parentDeptName);
            }
            if (isActive != null) {
                filters.put("isActive", isActive);
            }
            if (deptLevel != null) {
                filters.put("deptLevel", deptLevel);
            }

            Page<Department> result = departmentService.getDepartmentList(page, size, filters);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询部门列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有部门（树形结构）
     * 
     * @return 部门树形列表
     */
    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree() {
        try {
            List<Department> result = departmentService.getDepartmentTree();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询部门树失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有部门（树形结构，包含主管信息和人数统计）
     * 
     * @return 部门树形列表（包含主管姓名和部门人数）
     */
    @GetMapping("/tree-with-details")
    public Result<List<DepartmentTreeVO>> getDepartmentTreeWithDetails() {
        try {
            List<DepartmentTreeVO> result = departmentService.getDepartmentTreeWithDetails();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询部门树失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询部门详情
     * 
     * @param id 部门ID
     * @return 部门信息
     */
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        try {
            Department department = departmentService.getDepartmentById(id);
            if (department == null) {
                return Result.error(404, "部门不存在");
            }
            return Result.success(department);
        } catch (Exception e) {
            return Result.error(500, "查询部门详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新部门信息
     * 
     * @param id 部门ID
     * @param department 部门信息
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department,
            HttpServletRequest request) {
        try {
            // 设置部门ID
            department.setId(id);
            
            // 更新部门信息
            Department updated = departmentService.updateDepartment(department);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "更新部门信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新部门人数统计
     * 
     * @return 更新结果
     */
    @PostMapping("/update-employee-count")
    public Result<Object> updateEmployeeCount() {
        try {
            int count = departmentService.updateEmployeeCount();
            return Result.success("成功更新" + count + "个部门的人数统计");
        } catch (Exception e) {
            return Result.error(500, "更新部门人数统计失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用部门
     * 
     * @param id 部门ID
     * @param isActive 是否启用（1=启用，0=禁用）
     * @return 更新结果
     */
    @PutMapping("/{id}/active")
    public Result<Object> setDepartmentActive(
            @PathVariable Long id,
            @RequestParam Integer isActive) {
        try {
            boolean result = departmentService.setDepartmentActive(id, isActive);
            if (result) {
                return Result.success(isActive == 1 ? "部门已启用" : "部门已禁用");
            } else {
                return Result.error(500, "更新部门状态失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "更新部门状态失败：" + e.getMessage());
        }
    }
}

