package com.microport.healthcert.controller;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.dto.DingTalkConfigDTO;
import com.microport.healthcert.dto.EmailConfigDTO;
import com.microport.healthcert.dto.ReminderConfigDTO;
import com.microport.healthcert.dto.SyncResultDTO;
import com.microport.healthcert.service.ConfigService;
import com.microport.healthcert.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 系统配置控制器
 * 提供系统配置管理接口
 * 
 * @author system
 * @date 2024
 */
@RestController
@RequestMapping("/api/admin/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @Autowired
    private SyncService syncService;

    /**
     * 获取邮件配置
     * 
     * @return 邮件配置
     */
    @GetMapping("/email")
    public Result<EmailConfigDTO> getEmailConfig() {
        try {
            EmailConfigDTO config = configService.getEmailConfig();
            return Result.success(config);
        } catch (Exception e) {
            return Result.error(500, "获取邮件配置失败：" + e.getMessage());
        }
    }

    /**
     * 保存邮件配置
     * 
     * @param configDTO 邮件配置DTO
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 保存结果
     */
    @PostMapping("/email")
    public Result<Object> saveEmailConfig(@RequestBody EmailConfigDTO configDTO, HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            configService.saveEmailConfig(configDTO, adminId, adminName);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "保存邮件配置失败：" + e.getMessage());
        }
    }

    /**
     * 测试邮件发送
     * 
     * @param toEmail 测试邮箱地址
     * @return 测试结果
     */
    @PostMapping("/email/test")
    public Result<Object> testEmail(@RequestParam("toEmail") String toEmail) {
        try {
            configService.testEmail(toEmail);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "测试邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 获取钉钉配置
     * 
     * @return 钉钉配置
     */
    @GetMapping("/dingtalk")
    public Result<DingTalkConfigDTO> getDingTalkConfig() {
        try {
            DingTalkConfigDTO config = configService.getDingTalkConfig();
            return Result.success(config);
        } catch (Exception e) {
            return Result.error(500, "获取钉钉配置失败：" + e.getMessage());
        }
    }

    /**
     * 保存钉钉配置
     * 
     * @param configDTO 钉钉配置DTO
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 保存结果
     */
    @PostMapping("/dingtalk")
    public Result<Object> saveDingTalkConfig(@RequestBody DingTalkConfigDTO configDTO, HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            configService.saveDingTalkConfig(configDTO, adminId, adminName);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "保存钉钉配置失败：" + e.getMessage());
        }
    }

    /**
     * 获取提醒规则配置
     * 
     * @return 提醒规则配置
     */
    @GetMapping("/reminder")
    public Result<ReminderConfigDTO> getReminderConfig() {
        try {
            ReminderConfigDTO config = configService.getReminderConfig();
            return Result.success(config);
        } catch (Exception e) {
            return Result.error(500, "获取提醒规则配置失败：" + e.getMessage());
        }
    }

    /**
     * 保存提醒规则配置
     * 
     * @param configDTO 提醒规则配置DTO
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 保存结果
     */
    @PostMapping("/reminder")
    public Result<Object> saveReminderConfig(@RequestBody ReminderConfigDTO configDTO, HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            configService.saveReminderConfig(configDTO, adminId, adminName);
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "保存提醒规则配置失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发员工和部门同步
     * 同步远程hr_sync表的数据到本地employees和departments表
     * 注意：人员同步会自动包含部门同步
     * 
     * @param request HTTP请求对象（用于获取管理员信息）
     * @return 同步结果
     */
    @PostMapping("/sync")
    public Result<SyncResultDTO> manualSync(HttpServletRequest request) {
        try {
            // 从request attribute获取管理员信息（由JWT拦截器设置）
            Long adminId = (Long) request.getAttribute("userId");
            String adminName = (String) request.getAttribute("username");

            if (adminId == null || adminName == null) {
                return Result.error(401, "未登录");
            }

            // 检查是否正在同步（通过SyncLockInterceptor，如果正在同步会返回503）
            // 这里直接执行同步，SyncLockInterceptor会处理并发控制
            
            // 执行同步（包含员工和部门同步）
            SyncResultDTO result = syncService.syncEmployees();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "同步失败：" + e.getMessage());
        }
    }
}

