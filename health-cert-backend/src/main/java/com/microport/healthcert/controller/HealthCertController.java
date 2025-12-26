package com.microport.healthcert.controller;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.dto.HealthCertDTO;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.service.HealthCertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 健康证控制器（员工端）
 * 提供健康证上传、提交、查询等功能
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/health-cert")
public class HealthCertController {

    @Autowired
    private HealthCertService healthCertService;

    /**
     * 上传图片
     * 保存文件，调用OCR识别，返回图片路径和OCR结果
     * 
     * @param image 图片文件
     * @return 包含图片路径和OCR结果的响应
     */
    @PostMapping("/upload")
    public Result<HealthCertDTO> upload(@RequestParam("image") MultipartFile image) {
        try {
            HealthCertDTO result = healthCertService.uploadAndOcr(image);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 提交健康证
     * 验证编号唯一性，验证所有必填字段，保存健康证记录(状态为pending)，记录操作日志
     * 
     * @param dto 健康证数据
     * @param request HTTP请求对象（用于获取用户信息）
     * @return 提交结果
     */
    @PostMapping("/submit")
    public Result<Object> submit(@RequestBody HealthCertDTO dto, HttpServletRequest request) {
        try {
            // 从request attribute获取用户信息（由JWT拦截器设置）
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");

            if (userId == null || username == null) {
                return Result.error(401, "未登录");
            }

            healthCertService.submitHealthCert(dto, userId, username);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询我的健康证列表
     * 
     * @param request HTTP请求对象（用于获取用户信息）
     * @return 健康证列表
     */
    @GetMapping("/my-list")
    public Result<List<HealthCertificate>> getMyList(HttpServletRequest request) {
        try {
            // 从request attribute获取用户信息（由JWT拦截器设置）
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return Result.error(401, "未登录");
            }

            List<HealthCertificate> list = healthCertService.getMyHealthCertList(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }
}

