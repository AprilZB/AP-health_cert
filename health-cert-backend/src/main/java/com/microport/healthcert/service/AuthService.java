package com.microport.healthcert.service;

import com.microport.healthcert.dto.LoginDTO;
import com.microport.healthcert.vo.LoginVO;

/**
 * 认证服务接口
 * 提供登录、登出、获取当前用户等功能
 * 
 * @author system
 * @date 2024
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求DTO
     * @return 登录响应VO（包含token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     * 
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息（根据用户类型返回Admin或Employee信息）
     */
    Object getCurrentUser(Long userId);
}

