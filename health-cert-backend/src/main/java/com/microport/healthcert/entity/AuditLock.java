package com.microport.healthcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核锁实体类
 * 对应数据库表: audit_locks
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("audit_locks")
public class AuditLock {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 健康证ID
     */
    @TableField("cert_id")
    private Long certId;

    /**
     * 锁定管理员ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 锁定管理员姓名
     */
    @TableField("admin_name")
    private String adminName;

    /**
     * 锁定时间
     */
    @TableField("locked_at")
    private LocalDateTime lockedAt;

    /**
     * 锁定过期时间(5分钟后自动释放)
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;
}

