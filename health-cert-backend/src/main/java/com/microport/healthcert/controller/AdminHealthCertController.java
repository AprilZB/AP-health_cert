package com.microport.healthcert.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.common.Result;
import com.microport.healthcert.dto.AuditDTO;
import com.microport.healthcert.dto.ExportRequestDTO;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.service.AdminHealthCertService;
import com.microport.healthcert.service.ExportService;
import com.microport.healthcert.service.impl.ExportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员健康证控制器
 * 提供健康证审核相关接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/health-cert")
public class AdminHealthCertController {

    @Autowired
    private AdminHealthCertService adminHealthCertService;

    @Autowired
    private ExportService exportService;

    /**
     * 获取待审核列表
     * 
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @param employeeName 员工姓名（可选，模糊查询）
     * @param certNumber 健康证编号（可选，模糊查询）
     * @return 待审核健康证列表
     */
    @GetMapping("/pending")
    public Result<Page<HealthCertificate>> getPendingList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String certNumber) {
        try {
            // 构建筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (employeeName != null && !employeeName.trim().isEmpty()) {
                filters.put("employeeName", employeeName);
            }
            if (certNumber != null && !certNumber.trim().isEmpty()) {
                filters.put("certNumber", certNumber);
            }

            Page<HealthCertificate> result = adminHealthCertService.getPendingList(page, size, filters);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 锁定健康证
     * 
     * @param id 健康证ID
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 锁定结果
     */
    @PostMapping("/lock/{id}")
    public Result<Object> lockCertificate(@PathVariable("id") Long id, HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            adminHealthCertService.lockCertificate(id, adminId, adminName);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "锁定失败：" + e.getMessage());
        }
    }

    /**
     * 审核健康证
     * 
     * @param id 健康证ID
     * @param auditDTO 审核请求DTO
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 审核结果
     */
    @PostMapping("/audit/{id}")
    public Result<Object> auditCertificate(
            @PathVariable("id") Long id,
            @RequestBody AuditDTO auditDTO,
            HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            adminHealthCertService.auditCertificate(
                    id,
                    auditDTO.getAction(),
                    auditDTO.getReason(),
                    adminId,
                    adminName
            );
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "审核失败：" + e.getMessage());
        }
    }

    /**
     * 获取全部列表（支持筛选）
     * 
     * @param page 页码（可选，默认1）
     * @param size 每页大小（可选，默认10）
     * @param status 状态（可选，精确查询）
     * @param employeeName 员工姓名（可选，模糊查询）
     * @param certNumber 健康证编号（可选，模糊查询）
     * @param sfUserId 员工域账号（可选，精确查询）
     * @return 健康证列表
     */
    @GetMapping("/list")
    public Result<Page<HealthCertificate>> getAllList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String certNumber,
            @RequestParam(required = false) String sfUserId) {
        try {
            // 构建筛选条件
            Map<String, Object> filters = new HashMap<>();
            if (status != null && !status.trim().isEmpty()) {
                filters.put("status", status);
            }
            if (employeeName != null && !employeeName.trim().isEmpty()) {
                filters.put("employeeName", employeeName);
            }
            if (certNumber != null && !certNumber.trim().isEmpty()) {
                filters.put("certNumber", certNumber);
            }
            if (sfUserId != null && !sfUserId.trim().isEmpty()) {
                filters.put("sfUserId", sfUserId);
            }

            Page<HealthCertificate> result = adminHealthCertService.getAllList(page, size, filters);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出健康证
     * 
     * @param requestDTO 导出请求DTO
     * @return 下载URL
     */
    @PostMapping("/export")
    public Result<String> export(@RequestBody ExportRequestDTO requestDTO) {
        try {
            String downloadUrl;
            String format = requestDTO.getFormat() != null ? requestDTO.getFormat().toLowerCase() : "excel";
            Boolean includeImages = requestDTO.getIncludeImages() != null ? requestDTO.getIncludeImages() : false;

            if ("pdf".equals(format)) {
                downloadUrl = exportService.exportPdf(requestDTO.getFilters(), includeImages);
            } else {
                downloadUrl = exportService.exportExcel(requestDTO.getFilters(), includeImages);
            }

            return Result.success(downloadUrl);
        } catch (Exception e) {
            return Result.error(500, "导出失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件
     * 
     * @param fileName 文件名
     * @return 文件资源
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable("fileName") String fileName) {
        try {
            // 检查文件URL是否过期
            if (!ExportServiceImpl.isFileUrlValid(fileName)) {
                return ResponseEntity.notFound().build();
            }

            // 获取文件路径
            String userDir = System.getProperty("user.dir");
            String filePath = userDir + File.separator + "downloads" + File.separator + fileName;
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            // 根据文件扩展名设置Content-Type
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".xlsx")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

