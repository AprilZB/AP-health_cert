package com.microport.healthcert.service;

/**
 * 提醒服务接口
 * 提供健康证到期提醒功能
 * 
 * @author system
 * @date 2024
 */
public interface ReminderService {

    /**
     * 发送提醒
     * 查询即将到期的健康证（30/15/7/3/1天）和已过期的健康证（每周提醒一次），调用EmailService和DingTalkService发送
     */
    void sendReminders();
}

