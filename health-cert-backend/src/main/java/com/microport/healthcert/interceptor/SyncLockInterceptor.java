package com.microport.healthcert.interceptor;

import com.microport.healthcert.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microport.healthcert.service.impl.SyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 同步锁拦截器
 * 检查全局同步标志，如果正在同步，返回503错误
 * 
 * @author system
 * @date 2024
 */
@Component
public class SyncLockInterceptor implements HandlerInterceptor {

    @Autowired
    private SyncServiceImpl syncService;

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
        // 检查是否正在同步
        if (syncService.isSyncInProgress()) {
            // 正在同步，返回503错误
            return handleServiceUnavailable(response, "系统正在同步员工数据，请稍后再试");
        }

        // 未在同步，继续处理请求
        return true;
    }

    /**
     * 处理服务不可用情况，返回503错误
     * 
     * @param response HTTP响应对象
     * @param message 提示消息
     * @return false，中断请求处理
     * @throws Exception 异常
     */
    private boolean handleServiceUnavailable(HttpServletResponse response, String message) throws Exception {
        // 设置响应状态码为503（服务不可用）
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/json;charset=UTF-8");

        // 创建错误响应
        Result<Object> result = Result.error(503, message);

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

