package com.microport.healthcert.interceptor;

import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 日志拦截器
 * 拦截所有请求，记录请求URL、方法、参数、IP，记录响应结果和耗时
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Autowired
    private OperationLogMapper operationLogMapper;

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
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        // 记录请求信息
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        String clientIp = getClientIp(request);

        // 获取请求参数
        String requestParams = getRequestParams(request);

        // 将请求信息存储到request attribute中，供后置处理使用
        request.setAttribute("requestUrl", requestUrl);
        request.setAttribute("requestMethod", requestMethod);
        request.setAttribute("clientIp", clientIp);
        request.setAttribute("requestParams", requestParams);

        return true;
    }

    /**
     * 请求处理后的拦截逻辑
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @param ex 异常对象（如果有）
     * @throws Exception 异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 获取请求开始时间
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime == null) {
                return;
            }

            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 获取请求信息
            String requestUrl = (String) request.getAttribute("requestUrl");
            String requestMethod = (String) request.getAttribute("requestMethod");
            String clientIp = (String) request.getAttribute("clientIp");
            String requestParams = (String) request.getAttribute("requestParams");

            // 获取响应信息
            int statusCode = response.getStatus();
            String responseResult = statusCode >= 200 && statusCode < 300 ? "success" : "error";

            // 获取用户信息（由JWT拦截器设置）
            Long userId = (Long) request.getAttribute("userId");
            String userName = (String) request.getAttribute("username");
            String userType = (String) request.getAttribute("userType");

            // 如果用户未登录，使用默认值
            if (userId == null) {
                userId = 0L;
                userName = "anonymous";
                userType = "anonymous";
            }

            // 构建操作描述
            StringBuilder description = new StringBuilder();
            description.append("请求URL: ").append(requestUrl).append(", ");
            description.append("请求方法: ").append(requestMethod).append(", ");
            description.append("客户端IP: ").append(clientIp).append(", ");
            if (requestParams != null && !requestParams.isEmpty()) {
                description.append("请求参数: ").append(requestParams).append(", ");
            }
            description.append("响应状态: ").append(statusCode).append(", ");
            description.append("耗时: ").append(duration).append("ms");

            // 保存操作日志
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setUserType(userType);
            log.setOperation("api_request");
            log.setDescription(description.toString());
            log.setResult(responseResult);
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);

        } catch (Exception e) {
            // 记录日志失败不影响请求处理
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取请求参数
     * 
     * @param request HTTP请求对象
     * @return 请求参数字符串
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap == null || parameterMap.isEmpty()) {
                return "";
            }

            StringBuilder params = new StringBuilder();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                if (values != null && values.length > 0) {
                    params.append(key).append("=").append(String.join(",", values)).append("&");
                }
            }

            String result = params.toString();
            if (result.endsWith("&")) {
                result = result.substring(0, result.length() - 1);
            }

            // 限制参数长度，避免过长
            if (result.length() > 500) {
                result = result.substring(0, 500) + "...";
            }

            return result;
        } catch (Exception e) {
            log.warn("获取请求参数失败", e);
            return "";
        }
    }
}

