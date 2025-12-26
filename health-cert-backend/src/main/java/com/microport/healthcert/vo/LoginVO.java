package com.microport.healthcert.vo;

import lombok.Data;

/**
 * 登录响应VO
 * 包含登录成功后的Token和用户信息
 * 
 * @author system
 * @date 2024
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型（employee/admin）
     */
    private String userType;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱地址
     */
    private String email;
}

