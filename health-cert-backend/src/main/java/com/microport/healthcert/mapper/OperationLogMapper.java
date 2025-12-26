package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 * 对应实体类: OperationLog
 * 对应数据库表: operation_logs
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}

