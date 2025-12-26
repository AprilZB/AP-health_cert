package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 邮件配置DTO
 * 
 * @author system
 * @date 2024
 */
@Data
public class EmailConfigDTO {

    /**
     * SMTP服务器地址
     */
    private String smtpHost;

    /**
     * SMTP端口
     */
    private String smtpPort;

    /**
     * SMTP用户名
     */
    private String username;

    /**
     * SMTP密码
     */
    private String password;

    /**
     * 发件人邮箱
     */
    private String from;

    /**
     * 邮件主题模板
     */
    private String subjectTemplate;

    /**
     * 邮件内容模板
     */
    private String contentTemplate;
}

