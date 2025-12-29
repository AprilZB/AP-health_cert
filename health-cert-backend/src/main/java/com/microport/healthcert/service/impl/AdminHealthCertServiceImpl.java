package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microport.healthcert.entity.AuditLock;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.mapper.AuditLockMapper;
import com.microport.healthcert.mapper.HealthCertificateMapper;
import com.microport.healthcert.service.AdminHealthCertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员健康证服务实现类
 * 
 * @author system
 * @date 2024
 */
@Service
public class AdminHealthCertServiceImpl implements AdminHealthCertService {

    @Autowired
    private HealthCertificateMapper healthCertificateMapper;

    @Autowired
    private AuditLockMapper auditLockMapper;

    /**
     * 锁定过期时间（分钟）
     */
    private static final int LOCK_EXPIRY_MINUTES = 5;

    /**
     * 锁定健康证
     * 检查是否已被其他管理员锁定，创建audit_locks记录（5分钟后过期）
     * 
     * @param certId 健康证ID
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     */
    @Override
    public void lockCertificate(Long certId, Long adminId, String adminName) {
        // 检查健康证是否存在
        HealthCertificate healthCert = healthCertificateMapper.selectById(certId);
        if (healthCert == null) {
            throw new RuntimeException("健康证不存在");
        }

        // 检查是否已被其他管理员锁定
        LambdaQueryWrapper<AuditLock> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(AuditLock::getCertId, certId)
                   .gt(AuditLock::getExpiresAt, LocalDateTime.now()); // 只查询未过期的锁
        AuditLock existingLock = auditLockMapper.selectOne(lockWrapper);

        if (existingLock != null) {
            // 如果锁是自己创建的，允许重新锁定（更新过期时间）
            if (existingLock.getAdminId().equals(adminId)) {
                // 更新锁的过期时间
                existingLock.setExpiresAt(LocalDateTime.now().plusMinutes(LOCK_EXPIRY_MINUTES));
                auditLockMapper.updateById(existingLock);
                return;
            } else {
                // 被其他管理员锁定，抛出异常
                throw new RuntimeException("该健康证已被管理员" + existingLock.getAdminName() + "锁定，请稍后再试");
            }
        }

        // 创建新的锁定记录
        AuditLock auditLock = new AuditLock();
        auditLock.setCertId(certId);
        auditLock.setAdminId(adminId);
        auditLock.setAdminName(adminName);
        auditLock.setLockedAt(LocalDateTime.now());
        auditLock.setExpiresAt(LocalDateTime.now().plusMinutes(LOCK_EXPIRY_MINUTES)); // 5分钟后过期

        auditLockMapper.insert(auditLock);
    }

    /**
     * 审核健康证
     * 
     * @param certId 健康证ID
     * @param action 审核动作（approve/reject）
     * @param reason 拒绝原因（reject时必填）
     * @param adminId 审核人ID
     * @param adminName 审核人姓名
     */
    @Override
    public void auditCertificate(Long certId, String action, String reason, Long adminId, String adminName) {
        // 检查健康证是否存在
        HealthCertificate healthCert = healthCertificateMapper.selectById(certId);
        if (healthCert == null) {
            throw new RuntimeException("健康证不存在");
        }

        // 检查是否被锁定（且锁未过期）
        LambdaQueryWrapper<AuditLock> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(AuditLock::getCertId, certId)
                   .gt(AuditLock::getExpiresAt, LocalDateTime.now());
        AuditLock auditLock = auditLockMapper.selectOne(lockWrapper);

        if (auditLock == null) {
            throw new RuntimeException("健康证未被锁定，请先锁定");
        }

        // 检查锁是否是当前管理员创建的
        if (!auditLock.getAdminId().equals(adminId)) {
            throw new RuntimeException("该健康证已被其他管理员锁定，无法审核");
        }

        // 验证审核动作
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new IllegalArgumentException("审核动作必须是approve或reject");
        }

        // 如果是拒绝，必须提供原因
        if ("reject".equals(action) && (reason == null || reason.trim().isEmpty())) {
            throw new IllegalArgumentException("拒绝审核必须提供原因");
        }

        // 更新健康证状态
        healthCert.setStatus("approve".equals(action) ? "approved" : "rejected");
        healthCert.setAuditTime(LocalDateTime.now());
        healthCert.setAuditorId(adminId);
        healthCert.setAuditorName(adminName);
        
        if ("approve".equals(action)) {
            // 审核通过：将同一员工的其他健康证设为is_current=0，当前健康证设为is_current=1
            // 保证同一员工只有一个is_current=1的健康证
            LambdaQueryWrapper<HealthCertificate> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(HealthCertificate::getEmployeeId, healthCert.getEmployeeId())
                        .eq(HealthCertificate::getIsCurrent, 1)
                        .ne(HealthCertificate::getId, certId); // 排除当前健康证
            
            List<HealthCertificate> otherCerts = healthCertificateMapper.selectList(updateWrapper);
            for (HealthCertificate otherCert : otherCerts) {
                otherCert.setIsCurrent(0);
                otherCert.setUpdatedAt(LocalDateTime.now());
                healthCertificateMapper.updateById(otherCert);
            }
            
            // 将当前健康证设为is_current=1
            healthCert.setIsCurrent(1);
            healthCert.setRejectReason(null); // 清空拒绝原因（如果之前有）
        } else {
            // 审核拒绝：设置拒绝原因，is_current保持不变（不改变）
            healthCert.setRejectReason(reason);
            // is_current保持原值不变
        }

        healthCertificateMapper.updateById(healthCert);

        // 释放锁（删除锁定记录）
        auditLockMapper.deleteById(auditLock.getId());

        // 发送通知（暂时不实现，留TODO）
        // TODO: 发送审核结果通知给员工
    }

    /**
     * 获取待审核列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的待审核健康证列表
     */
    @Override
    public Page<HealthCertificate> getPendingList(Integer page, Integer size, Map<String, Object> filters) {
        // 创建分页对象
        Page<HealthCertificate> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 构建查询条件
        LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthCertificate::getStatus, "pending") // 只查询待审核状态
               .orderByDesc(HealthCertificate::getSubmitTime); // 按提交时间倒序

        // 应用筛选条件
        if (filters != null) {
            if (filters.containsKey("employeeName")) {
                wrapper.like(HealthCertificate::getEmployeeName, filters.get("employeeName"));
            }
            if (filters.containsKey("certNumber")) {
                wrapper.like(HealthCertificate::getCertNumber, filters.get("certNumber"));
            }
        }

        // 执行查询
        return healthCertificateMapper.selectPage(pageObj, wrapper);
    }

    /**
     * 获取全部列表（支持筛选）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param filters 筛选条件
     * @return 分页的健康证列表
     */
    @Override
    public Page<HealthCertificate> getAllList(Integer page, Integer size, Map<String, Object> filters) {
        // 创建分页对象
        Page<HealthCertificate> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 构建查询条件
        LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(HealthCertificate::getCreatedAt); // 按创建时间倒序

        // 应用筛选条件
        if (filters != null) {
            if (filters.containsKey("status")) {
                wrapper.eq(HealthCertificate::getStatus, filters.get("status"));
            }
            if (filters.containsKey("employeeName")) {
                wrapper.like(HealthCertificate::getEmployeeName, filters.get("employeeName"));
            }
            if (filters.containsKey("certNumber")) {
                wrapper.like(HealthCertificate::getCertNumber, filters.get("certNumber"));
            }
            if (filters.containsKey("sfUserId")) {
                wrapper.eq(HealthCertificate::getSfUserId, filters.get("sfUserId"));
            }
        }

        // 执行查询
        return healthCertificateMapper.selectPage(pageObj, wrapper);
    }
}

