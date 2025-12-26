package com.microport.healthcert.service;

import java.util.Map;

/**
 * 导出服务接口
 * 提供Excel和PDF导出功能
 * 
 * @author system
 * @date 2024
 */
public interface ExportService {

    /**
     * 导出Excel
     * 使用POI创建Excel，如果includeImages=true则嵌入健康证图片
     * 生成临时文件到downloads目录，返回下载URL（1小时后过期）
     * 
     * @param filters 筛选条件
     * @param includeImages 是否包含图片
     * @return 下载URL
     */
    String exportExcel(Map<String, Object> filters, Boolean includeImages);

    /**
     * 导出PDF
     * 使用iText创建PDF，嵌入健康证图片
     * 生成临时文件到downloads目录，返回下载URL（1小时后过期）
     * 
     * @param filters 筛选条件
     * @param includeImages 是否包含图片
     * @return 下载URL
     */
    String exportPdf(Map<String, Object> filters, Boolean includeImages);
}

