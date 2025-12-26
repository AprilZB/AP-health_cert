package com.microport.healthcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体类
 * 对应数据库表: departments
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("departments")
public class Department {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 上级部门名称
     */
    @TableField("parent_dept_name")
    private String parentDeptName;

    /**
     * 上级部门ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 部门层级(1根部门)
     */
    @TableField("dept_level")
    private Integer deptLevel;

    /**
     * 部门路径(用于树形查询)
     */
    @TableField("dept_path")
    private String deptPath;

    /**
     * 部门人数(统计用)
     */
    @TableField("employee_count")
    private Integer employeeCount;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

