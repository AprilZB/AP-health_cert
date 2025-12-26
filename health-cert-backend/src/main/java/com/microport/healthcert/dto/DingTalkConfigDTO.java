package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 钉钉配置DTO
 * 
 * @author system
 * @date 2024
 */
@Data
public class DingTalkConfigDTO {

    /**
     * 企业ID
     */
    private String corpId;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * 应用Key
     */
    private String appKey;
}

