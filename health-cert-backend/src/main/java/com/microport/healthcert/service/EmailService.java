package com.microport.healthcert.service;

import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.HealthCertificate;

/**
 * 邮件服务接口
 * 提供邮件发送功能
 * 
 * @author system
 * @date 2024
 */
public interface EmailService {

    /**
     * 发送邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendEmail(String to, String subject, String content);

    /**
     * 发送健康证到期提醒邮件
     * 
     * @param employee 员工信息
     * @param cert 健康证信息
     */
    void sendReminderEmail(Employee employee, HealthCertificate cert);
}

