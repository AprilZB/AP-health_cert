package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 提醒规则配置DTO
 * 
 * @author system
 * @date 2024
 */
@Data
public class ReminderConfigDTO {

    /**
     * 提醒天数（逗号分隔，如：30,15,7,3,1）
     */
    private String reminderDays;

    /**
     * 是否启用邮件提醒
     */
    private Boolean emailEnabled;

    /**
     * 是否启用钉钉提醒
     */
    private Boolean dingtalkEnabled;
}

