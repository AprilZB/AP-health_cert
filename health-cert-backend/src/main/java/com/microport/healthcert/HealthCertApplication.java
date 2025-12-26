package com.microport.healthcert;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 员工健康证管理系统 - 主启动类
 * 
 * 注意：
 * 1. 禁用MyBatis-Plus和DataSource的自动配置，完全手动配置
 * 2. Mapper扫描配置已移至LocalDataSourceConfig和RemoteDataSourceConfig
 *    - LocalDataSourceConfig：扫描本地Mapper（com.microport.healthcert.mapper包），使用本地数据源
 *    - RemoteDataSourceConfig：扫描远程Mapper（com.microport.healthcert.mapper.remote包），使用远程数据源
 * 3. 禁用DataSource自动配置，手动配置数据源
 * 
 * @author system
 * @date 2024
 */
@SpringBootApplication(exclude = {
    MybatisPlusAutoConfiguration.class,
    DataSourceAutoConfiguration.class
})
@EnableScheduling
public class HealthCertApplication {

    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HealthCertApplication.class, args);
    }
}

