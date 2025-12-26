package com.microport.healthcert.service;

import com.microport.healthcert.dto.HealthCertDTO;

import java.io.File;

/**
 * OCR服务接口
 * 提供OCR识别功能
 * 
 * @author system
 * @date 2024
 */
public interface OcrService {

    /**
     * 调用OCR API识别健康证
     * 
     * @param imageFile 图片文件
     * @return OCR识别结果（HealthCertDTO）
     */
    HealthCertDTO callOcrApi(File imageFile);

    /**
     * 解析OCR结果为HealthCertDTO
     * 
     * @param ocrJson OCR返回的JSON字符串
     * @return 解析后的HealthCertDTO
     */
    HealthCertDTO parseOcrResult(String ocrJson);
}

