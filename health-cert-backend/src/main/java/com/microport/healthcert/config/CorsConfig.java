package com.microport.healthcert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 配置允许所有来源的跨域请求，解决前后端分离开发中的跨域问题
 * 
 * @author system
 * @date 2024
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 允许所有来源、所有请求方法、所有请求头
     * 
     * @return CorsFilter对象
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        
        // 允许所有请求方法（GET、POST、PUT、DELETE等）
        config.addAllowedMethod("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许携带凭证（如Cookie）
        config.setAllowCredentials(true);
        
        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        // 返回CORS过滤器
        return new CorsFilter(source);
    }
}

