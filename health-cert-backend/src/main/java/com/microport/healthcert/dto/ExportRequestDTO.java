package com.microport.healthcert.dto;

import lombok.Data;

import java.util.Map;

/**
 * 导出请求DTO
 * 用于接收导出请求参数
 * 
 * @author system
 * @date 2024
 */
@Data
public class ExportRequestDTO {

    /**
     * 筛选条件
     */
    private Map<String, Object> filters;

    /**
     * 导出格式（excel/pdf）
     */
    private String format;

    /**
     * 是否包含图片（true/false）
     */
    private Boolean includeImages;
}

