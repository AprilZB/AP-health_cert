package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 同步结果DTO
 * 包含同步统计信息
 * 
 * @author system
 * @date 2024
 */
@Data
public class SyncResultDTO {

    /**
     * 新增员工数
     */
    private Integer addedCount;

    /**
     * 更新员工数
     */
    private Integer updatedCount;

    /**
     * 离职员工数（标记为is_active=0）
     */
    private Integer inactiveCount;

    /**
     * 同步开始时间
     */
    private String startTime;

    /**
     * 同步结束时间
     */
    private String endTime;

    /**
     * 同步耗时（毫秒）
     */
    private Long duration;
}

