package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.dto.DingTalkConfigDTO;
import com.microport.healthcert.dto.EmailConfigDTO;
import com.microport.healthcert.dto.ReminderConfigDTO;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.entity.SystemConfig;
import com.microport.healthcert.mapper.OperationLogMapper;
import com.microport.healthcert.mapper.SystemConfigMapper;
import com.microport.healthcert.service.ConfigService;
import com.microport.healthcert.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统配置服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 获取邮件配置
     * 
     * @return 邮件配置DTO
     */
    @Override
    public EmailConfigDTO getEmailConfig() {
        EmailConfigDTO configDTO = new EmailConfigDTO();
        configDTO.setSmtpHost(getConfigValue("email.smtp.host"));
        configDTO.setSmtpPort(getConfigValue("email.smtp.port"));
        configDTO.setUsername(getConfigValue("email.username"));
        configDTO.setPassword(getConfigValue("email.password"));
        configDTO.setFrom(getConfigValue("email.from"));
        configDTO.setSubjectTemplate(getConfigValue("email.subject.template"));
        configDTO.setContentTemplate(getConfigValue("email.content.template"));
        return configDTO;
    }

    /**
     * 保存邮件配置
     * 
     * @param configDTO 邮件配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    @Override
    public void saveEmailConfig(EmailConfigDTO configDTO, Long adminId, String adminName) {
        saveConfig("email.smtp.host", configDTO.getSmtpHost(), "string", "邮件SMTP服务器地址", "email");
        saveConfig("email.smtp.port", configDTO.getSmtpPort(), "string", "邮件SMTP端口", "email");
        saveConfig("email.username", configDTO.getUsername(), "string", "邮件SMTP用户名", "email");
        saveConfig("email.password", configDTO.getPassword(), "string", "邮件SMTP密码", "email");
        saveConfig("email.from", configDTO.getFrom(), "string", "发件人邮箱", "email");
        saveConfig("email.subject.template", configDTO.getSubjectTemplate(), "string", "邮件主题模板", "email");
        saveConfig("email.content.template", configDTO.getContentTemplate(), "string", "邮件内容模板", "email");

        // 记录操作日志
        saveOperationLog(adminId, adminName, "admin", "config", "保存邮件配置", "success");
    }

    /**
     * 测试邮件发送
     * 
     * @param toEmail 测试邮箱地址
     */
    @Override
    public void testEmail(String toEmail) {
        try {
            emailService.sendEmail(toEmail, "健康证系统测试邮件", "这是一封测试邮件，如果您收到此邮件，说明邮件配置正确。");
            log.info("测试邮件发送成功，收件人：{}", toEmail);
        } catch (Exception e) {
            log.error("测试邮件发送失败，收件人：{}", toEmail, e);
            throw new RuntimeException("测试邮件发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取钉钉配置
     * 
     * @return 钉钉配置DTO
     */
    @Override
    public DingTalkConfigDTO getDingTalkConfig() {
        DingTalkConfigDTO configDTO = new DingTalkConfigDTO();
        configDTO.setCorpId(getConfigValue("dingtalk.corp_id"));
        configDTO.setAppSecret(getConfigValue("dingtalk.app_secret"));
        configDTO.setAppKey(getConfigValue("dingtalk.app_key"));
        return configDTO;
    }

    /**
     * 保存钉钉配置
     * 
     * @param configDTO 钉钉配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    @Override
    public void saveDingTalkConfig(DingTalkConfigDTO configDTO, Long adminId, String adminName) {
        saveConfig("dingtalk.corp_id", configDTO.getCorpId(), "string", "钉钉企业ID", "dingtalk");
        saveConfig("dingtalk.app_secret", configDTO.getAppSecret(), "string", "钉钉应用密钥", "dingtalk");
        saveConfig("dingtalk.app_key", configDTO.getAppKey(), "string", "钉钉应用Key", "dingtalk");

        // 记录操作日志
        saveOperationLog(adminId, adminName, "admin", "config", "保存钉钉配置", "success");
    }

    /**
     * 获取提醒规则配置
     * 
     * @return 提醒规则配置DTO
     */
    @Override
    public ReminderConfigDTO getReminderConfig() {
        ReminderConfigDTO configDTO = new ReminderConfigDTO();
        configDTO.setReminderDays(getConfigValue("reminder.days"));
        configDTO.setEmailEnabled("true".equalsIgnoreCase(getConfigValue("reminder.email.enabled")));
        configDTO.setDingtalkEnabled("true".equalsIgnoreCase(getConfigValue("reminder.dingtalk.enabled")));
        return configDTO;
    }

    /**
     * 保存提醒规则配置
     * 
     * @param configDTO 提醒规则配置DTO
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    @Override
    public void saveReminderConfig(ReminderConfigDTO configDTO, Long adminId, String adminName) {
        saveConfig("reminder.days", configDTO.getReminderDays(), "string", "提醒天数", "reminder");
        saveConfig("reminder.email.enabled", configDTO.getEmailEnabled() != null && configDTO.getEmailEnabled() ? "true" : "false", "string", "是否启用邮件提醒", "reminder");
        saveConfig("reminder.dingtalk.enabled", configDTO.getDingtalkEnabled() != null && configDTO.getDingtalkEnabled() ? "true" : "false", "string", "是否启用钉钉提醒", "reminder");

        // 记录操作日志
        saveOperationLog(adminId, adminName, "admin", "config", "保存提醒规则配置", "success");
    }

    /**
     * 从system_configs表读取配置值
     * 
     * @param configKey 配置键
     * @return 配置值，如果不存在返回null
     */
    private String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 保存配置到system_configs表
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @param configType 配置类型
     * @param description 配置描述
     * @param groupName 配置分组
     */
    private void saveConfig(String configKey, String configValue, String configType, String description, String groupName) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config == null) {
            // 配置不存在，创建新配置
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType(configType);
            config.setDescription(description);
            config.setGroupName(groupName);
            config.setIsEncrypted(0);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.insert(config);
        } else {
            // 配置已存在，更新配置值
            config.setConfigValue(configValue);
            config.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.updateById(config);
        }
    }

    /**
     * 保存操作日志
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param operation 操作类型
     * @param description 操作描述
     * @param result 操作结果
     */
    private void saveOperationLog(Long userId, String userName, String userType, String operation, String description, String result) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setUserType(userType);
            log.setOperation(operation);
            log.setDescription(description);
            log.setResult(result);
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}

