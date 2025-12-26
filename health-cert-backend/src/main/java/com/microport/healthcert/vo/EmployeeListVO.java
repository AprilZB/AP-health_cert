package com.microport.healthcert.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工列表VO
 * 用于下钻功能显示员工信息
 * 
 * @author system
 * @date 2024
 */
@Data
public class EmployeeListVO {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 工号
     */
    private String mpNumber;

    /**
     * 职位
     */
    private String jobName;

    /**
     * 健康证编号（如果有）
     */
    private String certNumber;

    /**
     * 健康证有效期至（如果有）
     */
    private LocalDate expiryDate;

    /**
     * 健康证状态（如果有）
     */
    private String certStatus;
}

