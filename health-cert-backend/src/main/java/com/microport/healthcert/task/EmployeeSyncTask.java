package com.microport.healthcert.task;

import com.microport.healthcert.dto.SyncResultDTO;
import com.microport.healthcert.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 员工同步定时任务
 * 定时执行员工数据同步
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Component
public class EmployeeSyncTask {

    @Autowired
    private SyncService syncService;

    /**
     * 定时同步员工数据
     * cron表达式: 0 0 0,12,18 * * ? (每天0点、12点、18点执行)
     */
    @Scheduled(cron = "0 0 0,12,18 * * ?")
    public void syncEmployees() {
        log.info("开始执行员工同步定时任务");
        try {
            SyncResultDTO result = syncService.syncEmployees();
            log.info("员工同步定时任务执行成功：新增{}条，更新{}条，离职{}条，耗时{}ms",
                    result.getAddedCount(),
                    result.getUpdatedCount(),
                    result.getInactiveCount(),
                    result.getDuration());
        } catch (Exception e) {
            log.error("员工同步定时任务执行失败", e);
        }
    }
}

