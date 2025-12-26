package com.microport.healthcert.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门树形结构VO
 * 用于前端树状显示部门信息
 *
 * @author system
 * @date 2024
 */
@Data
public class DepartmentTreeVO {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 上级部门名称
     */
    private String parentDeptName;

    /**
     * 上级部门ID
     */
    private Long parentId;

    /**
     * 部门层级(1根部门)
     */
    private Integer deptLevel;

    /**
     * 部门路径
     */
    private String deptPath;

    /**
     * 部门人数(仅本级部门，不包含下级部门)
     */
    private Integer employeeCount;

    /**
     * 主管姓名
     */
    private String supervisorName;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 子部门列表
     */
    private List<DepartmentTreeVO> children;

    /**
     * 构造函数
     */
    public DepartmentTreeVO() {
        this.children = new ArrayList<>();
    }
}

