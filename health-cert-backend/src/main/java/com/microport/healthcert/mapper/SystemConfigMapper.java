package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 * 对应实体类: SystemConfig
 * 对应数据库表: system_configs
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
}

