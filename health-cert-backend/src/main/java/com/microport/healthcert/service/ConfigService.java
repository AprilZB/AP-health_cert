package com.microport.healthcert.service;

import com.microport.healthcert.dto.DingTalkConfigDTO;
import com.microport.healthcert.dto.EmailConfigDTO;
import com.microport.healthcert.dto.ReminderConfigDTO;

/**
 * 系统配置服务接口
 * 提供系统配置管理功能
 * 
 * @author system
 * @date 2024
 */
public interface ConfigService {

    /**
     * 获取邮件配置
     * 
     * @return 邮件配置DTO
     */
    EmailConfigDTO getEmailConfig();

    /**
     * 保存邮件配置
     * 
     * @param configDTO 邮件配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    void saveEmailConfig(EmailConfigDTO configDTO, Long adminId, String adminName);

    /**
     * 测试邮件发送
     * 
     * @param toEmail 测试邮箱地址
     */
    void testEmail(String toEmail);

    /**
     * 获取钉钉配置
     * 
     * @return 钉钉配置DTO
     */
    DingTalkConfigDTO getDingTalkConfig();

    /**
     * 保存钉钉配置
     * 
     * @param configDTO 钉钉配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    void saveDingTalkConfig(DingTalkConfigDTO configDTO, Long adminId, String adminName);

    /**
     * 获取提醒规则配置
     * 
     * @return 提醒规则配置DTO
     */
    ReminderConfigDTO getReminderConfig();

    /**
     * 保存提醒规则配置
     * 
     * @param configDTO 提醒规则配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    void saveReminderConfig(ReminderConfigDTO configDTO, Long adminId, String adminName);
}

