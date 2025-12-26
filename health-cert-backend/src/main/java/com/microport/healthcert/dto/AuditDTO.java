package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 审核请求DTO
 * 用于接收审核请求参数
 * 
 * @author system
 * @date 2024
 */
@Data
public class AuditDTO {

    /**
     * 审核动作（approve/reject）
     */
    private String action;

    /**
     * 拒绝原因（reject时必填）
     */
    private String reason;
}

