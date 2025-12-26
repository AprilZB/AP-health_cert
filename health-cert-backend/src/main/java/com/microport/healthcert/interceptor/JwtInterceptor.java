package com.microport.healthcert.interceptor;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.common.ResultCode;
import com.microport.healthcert.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT拦截器
 * 拦截所有/api/**请求（除了/api/auth/login），验证JWT Token有效性
 * 
 * @author system
 * @date 2024
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 请求处理前的拦截逻辑
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return true表示继续处理，false表示中断处理
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestPath = request.getRequestURI();

        // 排除登录接口和健康检查接口，不需要验证token
        if ("/api/auth/login".equals(requestPath) || "/api/auth/health".equals(requestPath)) {
            return true;
        }

        // 从请求头获取Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有token，返回401错误
            return handleUnauthorized(response, "未提供Token");
        }

        // 提取token（去掉"Bearer "前缀）
        String token = authHeader.substring(7);

        // 验证token有效性
        if (!jwtUtil.validateToken(token)) {
            // token无效，返回401错误
            return handleUnauthorized(response, "Token无效或已过期");
        }

        // token有效，从token中提取用户信息并存入request attribute
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);

            // 将用户信息存入request attribute，供后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("userType", userType);
        } catch (Exception e) {
            // 解析token失败，返回401错误
            return handleUnauthorized(response, "Token解析失败");
        }

        // 验证通过，继续处理请求
        return true;
    }

    /**
     * 处理未授权情况，返回401错误
     * 
     * @param response HTTP响应对象
     * @param message 错误消息
     * @return false，中断请求处理
     * @throws Exception 异常
     */
    private boolean handleUnauthorized(HttpServletResponse response, String message) throws Exception {
        // 设置响应状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // 创建错误响应
        Result<Object> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);

        // 将响应结果写入响应流
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(result);

        PrintWriter writer = response.getWriter();
        writer.write(jsonResult);
        writer.flush();
        writer.close();

        // 返回false，中断请求处理
        return false;
    }
}

