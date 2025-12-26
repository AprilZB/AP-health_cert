package com.microport.healthcert.mapper.remote;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.remote.HrSync;
import org.apache.ibatis.annotations.Mapper;

/**
 * 远程HR同步员工信息Mapper接口
 * 对应实体类: HrSync
 * 对应远程数据库表: hr_sync (proposal_improvement数据库)
 * 注意: 此Mapper使用第二数据源(远程数据库)
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface HrSyncMapper extends BaseMapper<HrSync> {
}

