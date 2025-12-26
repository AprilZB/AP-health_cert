package com.microport.healthcert.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.OperationLog;

import java.util.Map;

/**
 * 操作日志服务接口
 * 提供操作日志管理功能
 * 
 * @author system
 * @date 2024
 */
public interface OperationLogService {

    /**
     * 保存日志
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param operation 操作类型
     * @param description 操作描述
     */
    void saveLog(Long userId, String userName, String userType, String operation, String description);

    /**
     * 查询日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的日志列表
     */
    Page<OperationLog> queryLogs(Integer page, Integer size, Map<String, Object> filters);

    /**
     * 导出日志
     * 
     * @param filters 筛选条件
     * @return 导出文件URL
     */
    String exportLogs(Map<String, Object> filters);
}

