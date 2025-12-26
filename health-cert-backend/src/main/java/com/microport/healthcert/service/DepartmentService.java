package com.microport.healthcert.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.Department;
import com.microport.healthcert.vo.DepartmentTreeVO;

import java.util.List;
import java.util.Map;

/**
 * 部门管理服务接口
 * 提供部门查询、更新等功能
 * 
 * @author system
 * @date 2024
 */
public interface DepartmentService {

    /**
     * 查询部门列表（支持分页和筛选）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件（部门名称、上级部门、状态等）
     * @return 分页的部门列表
     */
    Page<Department> getDepartmentList(Integer page, Integer size, Map<String, Object> filters);

    /**
     * 查询所有部门（树形结构）
     * 
     * @return 部门树形列表
     */
    List<Department> getDepartmentTree();

    /**
     * 查询所有部门（树形结构，包含主管信息和人数统计）
     * 
     * @return 部门树形列表（包含主管姓名和部门人数）
     */
    List<DepartmentTreeVO> getDepartmentTreeWithDetails();

    /**
     * 根据ID查询部门详情
     * 
     * @param id 部门ID
     * @return 部门信息
     */
    Department getDepartmentById(Long id);

    /**
     * 更新部门信息
     * 
     * @param department 部门信息
     * @return 更新后的部门信息
     */
    Department updateDepartment(Department department);

    /**
     * 更新部门人数统计
     * 根据employees表统计每个部门的人数
     * 
     * @return 更新的部门数量
     */
    int updateEmployeeCount();

    /**
     * 启用/禁用部门
     * 
     * @param id 部门ID
     * @param isActive 是否启用（1=启用，0=禁用）
     * @return 更新结果
     */
    boolean setDepartmentActive(Long id, Integer isActive);
}

