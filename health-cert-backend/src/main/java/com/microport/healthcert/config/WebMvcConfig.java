package com.microport.healthcert.config;

import com.microport.healthcert.interceptor.JwtInterceptor;
import com.microport.healthcert.interceptor.LogInterceptor;
import com.microport.healthcert.interceptor.SyncLockInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 用于注册拦截器和配置拦截路径
 * 
 * @author system
 * @date 2024
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private SyncLockInterceptor syncLockInterceptor;

    @Autowired
    private LogInterceptor logInterceptor;

    /**
     * 配置静态资源映射
     * 用于访问上传的图片文件
     * 
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String userDir = System.getProperty("user.dir");
        // 统一使用正斜杠作为路径分隔符（适用于Windows和Linux）
        String uploadPath = userDir.replace("\\", "/") + "/uploads/";
        
        // 映射 /uploads/** 路径到文件系统的 uploads 目录
        // 使用 file: 前缀指定文件系统路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        
        // 输出日志以便调试
        System.out.println("静态资源映射配置：/uploads/** -> file:" + uploadPath);
    }

    /**
     * 注册拦截器并配置拦截路径
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册日志拦截器（优先级最高，最先执行，最后完成）
        registry.addInterceptor(logInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                .order(-1); // 设置优先级为-1（数字越小优先级越高）

        // 注册同步锁拦截器（优先级高，先执行）
        registry.addInterceptor(syncLockInterceptor)
                // 拦截所有/api/**路径的请求
                .addPathPatterns("/api/**")
                // 排除/api/auth/login和/api/auth/health路径（登录和健康检查接口不需要检查同步状态）
                .excludePathPatterns("/api/auth/login", "/api/auth/health")
                .order(0); // 设置优先级为0（数字越小优先级越高）

        // 注册JWT拦截器
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有/api/**路径的请求
                .addPathPatterns("/api/**")
                // 排除/api/auth/login和/api/auth/health路径（登录和健康检查接口不需要验证token）
                .excludePathPatterns("/api/auth/login", "/api/auth/health")
                .order(1); // 设置优先级为1
    }
}

