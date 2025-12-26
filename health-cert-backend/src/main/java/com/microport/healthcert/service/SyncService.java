package com.microport.healthcert.service;

import com.microport.healthcert.dto.SyncResultDTO;

/**
 * 员工同步服务接口
 * 提供员工数据同步功能
 * 
 * @author system
 * @date 2024
 */
public interface SyncService {

    /**
     * 同步员工数据
     * 从远程hr_sync表同步到本地employees表
     * 
     * @return 同步结果（新增数/更新数/离职数）
     */
    SyncResultDTO syncEmployees();
}

