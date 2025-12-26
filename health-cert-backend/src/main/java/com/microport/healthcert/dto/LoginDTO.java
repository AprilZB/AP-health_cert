package com.microport.healthcert.dto;

import lombok.Data;

/**
 * 登录请求DTO
 * 用于接收登录请求参数
 * 
 * @author system
 * @date 2024
 */
@Data
public class LoginDTO {

    /**
     * 用户名（管理员账号或员工域账号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}

