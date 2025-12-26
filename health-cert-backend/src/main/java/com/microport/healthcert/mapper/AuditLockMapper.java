package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.AuditLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核锁Mapper接口
 * 对应实体类: AuditLock
 * 对应数据库表: audit_locks
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface AuditLockMapper extends BaseMapper<AuditLock> {
}

