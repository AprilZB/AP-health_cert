package com.microport.healthcert.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 日志清理定时任务
 * 定期清理1年以前的日志
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Component
public class LogCleanupTask {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 清理1年以前的日志
     * cron表达式: 0 0 2 * * ? (每天凌晨2点执行)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        log.info("开始执行日志清理任务");
        try {
            // 计算1年前的日期
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

            // 查询1年以前的日志
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(OperationLog::getCreatedAt, oneYearAgo);

            // 删除1年以前的日志
            int deletedCount = operationLogMapper.delete(wrapper);

            log.info("日志清理完成，删除{}条1年以前的日志", deletedCount);
        } catch (Exception e) {
            log.error("日志清理任务执行失败", e);
        }
    }
}

