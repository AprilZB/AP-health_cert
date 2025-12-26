package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper接口
 * 对应实体类: Admin
 * 对应数据库表: admins
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}

