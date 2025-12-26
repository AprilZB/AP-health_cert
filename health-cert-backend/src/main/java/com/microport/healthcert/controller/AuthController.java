package com.microport.healthcert.controller;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.dto.LoginDTO;
import com.microport.healthcert.service.AuthService;
import com.microport.healthcert.util.JwtUtil;
import com.microport.healthcert.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 * 提供登录、登出、获取当前用户等接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * 支持管理员和员工登录，员工首次登录会自动同步到本地
     * 
     * @param loginDTO 登录请求DTO
     * @return 登录响应结果（包含token和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户登出
     * 记录登出日志
     * 
     * @param request HTTP请求对象（用于获取token中的用户信息）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Object> logout(HttpServletRequest request) {
        // 从请求头获取token
        String token = getTokenFromRequest(request);
        if (token != null) {
            // 从token获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            authService.logout(userId);
        }
        return Result.success();
    }

    /**
     * 获取当前用户信息
     * 根据token获取当前登录用户的信息
     * 
     * @param request HTTP请求对象（用于获取token中的用户信息）
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Result<Object> getCurrentUser(HttpServletRequest request) {
        // 从请求头获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return Result.error(401, "未登录");
        }

        // 从token获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        Object user = authService.getCurrentUser(userId);
        return Result.success(user);
    }

    /**
     * 健康检查接口
     * 用于Docker健康检查，不需要认证
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public Result<Object> health() {
        return Result.success("服务运行正常");
    }

    /**
     * 从请求头获取Token
     * 
     * @param request HTTP请求对象
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

