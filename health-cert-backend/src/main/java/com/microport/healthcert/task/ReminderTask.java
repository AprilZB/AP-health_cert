package com.microport.healthcert.task;

import com.microport.healthcert.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 提醒定时任务
 * 定时执行健康证到期提醒
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Component
public class ReminderTask {

    @Autowired
    private ReminderService reminderService;

    /**
     * 定时发送健康证到期提醒
     * cron表达式: 0 0 9 * * ? (每天9点执行)
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        log.info("开始执行健康证到期提醒定时任务");
        try {
            reminderService.sendReminders();
            log.info("健康证到期提醒定时任务执行完成");
        } catch (Exception e) {
            log.error("健康证到期提醒定时任务执行失败", e);
        }
    }
}

