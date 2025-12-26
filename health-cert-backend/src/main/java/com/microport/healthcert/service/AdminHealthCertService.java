package com.microport.healthcert.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.HealthCertificate;

import java.util.Map;

/**
 * 管理员健康证服务接口
 * 提供健康证审核相关功能
 * 
 * @author system
 * @date 2024
 */
public interface AdminHealthCertService {

    /**
     * 锁定健康证
     * 检查是否已被其他管理员锁定，创建audit_locks记录（5分钟后过期）
     * 
     * @param certId 健康证ID
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    void lockCertificate(Long certId, Long adminId, String adminName);

    /**
     * 审核健康证
     * 
     * @param certId 健康证ID
     * @param action 审核动作（approve/reject）
     * @param reason 拒绝原因（reject时必填）
     * @param adminId 审核人ID
     * @param adminName 审核人姓名
     */
    void auditCertificate(Long certId, String action, String reason, Long adminId, String adminName);

    /**
     * 获取待审核列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的待审核健康证列表
     */
    Page<HealthCertificate> getPendingList(Integer page, Integer size, Map<String, Object> filters);

    /**
     * 获取全部列表（支持筛选）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的健康证列表
     */
    Page<HealthCertificate> getAllList(Integer page, Integer size, Map<String, Object> filters);
}

