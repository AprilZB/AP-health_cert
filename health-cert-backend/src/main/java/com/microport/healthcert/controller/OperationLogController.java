package com.microport.healthcert.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.common.Result;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志控制器
 * 提供操作日志查询和导出接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 查询操作日志列表
     * 
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @param userName 用户名（可选，模糊查询）
     * @param userType 用户类型（可选，精确查询）
     * @param operation 操作类型（可选，精确查询）
     * @param startTime 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @return 操作日志列表
     */
    @GetMapping("/list")
    public Result<Page<OperationLog>> getLogList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 构建筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (userName != null && !userName.trim().isEmpty()) {
                filters.put("userName", userName);
            }
            if (userType != null && !userType.trim().isEmpty()) {
                filters.put("userType", userType);
            }
            if (operation != null && !operation.trim().isEmpty()) {
                filters.put("operation", operation);
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                filters.put("startTime", startTime);
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                filters.put("endTime", endTime);
            }

            Page<OperationLog> result = operationLogService.queryLogs(page, size, filters);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询操作日志失败：" + e.getMessage());
        }
    }

    /**
     * 导出操作日志
     * 
     * @param userName 用户名（可选，模糊查询）
     * @param userType 用户类型（可选，精确查询）
     * @param operation 操作类型（可选，精确查询）
     * @param startTime 开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @return 导出文件URL
     */
    @PostMapping("/export")
    public Result<String> exportLogs(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            // 构建筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (userName != null && !userName.trim().isEmpty()) {
                filters.put("userName", userName);
            }
            if (userType != null && !userType.trim().isEmpty()) {
                filters.put("userType", userType);
            }
            if (operation != null && !operation.trim().isEmpty()) {
                filters.put("operation", operation);
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                filters.put("startTime", startTime);
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                filters.put("endTime", endTime);
            }

            String downloadUrl = operationLogService.exportLogs(filters);
            return Result.success(downloadUrl);
        } catch (Exception e) {
            return Result.error(500, "导出操作日志失败：" + e.getMessage());
        }
    }
}

