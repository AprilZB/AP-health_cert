package com.microport.healthcert.service;

import com.microport.healthcert.dto.HealthCertDTO;
import com.microport.healthcert.entity.HealthCertificate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 健康证服务接口
 * 
 * @author system
 * @date 2024
 */
public interface HealthCertService {

    /**
     * 上传图片并调用OCR识别
     * 
     * @param file 图片文件
     * @return 包含图片路径和OCR结果的DTO
     */
    HealthCertDTO uploadAndOcr(MultipartFile file);

    /**
     * 提交健康证
     * 
     * @param dto 健康证数据
     * @param userId 用户ID
     * @param username 用户名
     */
    void submitHealthCert(HealthCertDTO dto, Long userId, String username);

    /**
     * 查询我的健康证列表
     * 
     * @param userId 用户ID
     * @return 健康证列表
     */
    List<HealthCertificate> getMyHealthCertList(Long userId);

    /**
     * 根据ID查询健康证详情（员工端）
     * 只能查询自己的健康证
     * 
     * @param certId 健康证ID
     * @param userId 用户ID（用于验证权限）
     * @return 健康证详情
     */
    HealthCertificate getMyHealthCertById(Long certId, Long userId);
}

