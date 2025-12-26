package com.microport.healthcert.vo;

import lombok.Data;

/**
 * 数据看板概览统计VO
 * 
 * @author system
 * @date 2024
 */
@Data
public class DashboardOverviewVO {

    /**
     * 总员工数
     */
    private Integer totalEmployees;

    /**
     * 在职员工数
     */
    private Integer activeEmployees;

    /**
     * 已提交健康证数
     */
    private Integer submittedCount;

    /**
     * 待审核健康证数
     */
    private Integer pendingCount;

    /**
     * 已通过健康证数
     */
    private Integer approvedCount;

    /**
     * 即将到期数（30天）
     */
    private Integer expiring30Days;

    /**
     * 即将到期数（15天）
     */
    private Integer expiring15Days;

    /**
     * 即将到期数（7天）
     */
    private Integer expiring7Days;

    /**
     * 已过期数
     */
    private Integer expiredCount;

    /**
     * 覆盖率（百分比）
     */
    private Double coverageRate;

    /**
     * 没有健康证信息的员工数量
     */
    private Integer noCertCount;
}

