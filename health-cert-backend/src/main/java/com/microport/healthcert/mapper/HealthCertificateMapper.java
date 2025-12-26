package com.microport.healthcert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microport.healthcert.entity.HealthCertificate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康证信息Mapper接口
 * 对应实体类: HealthCertificate
 * 对应数据库表: health_certificates
 * 
 * @author system
 * @date 2024
 */
@Mapper
public interface HealthCertificateMapper extends BaseMapper<HealthCertificate> {
}

