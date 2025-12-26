package com.microport.healthcert.config;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 远程数据源配置类
 * 用于配置访问远程proposal_improvement数据库的数据源
 * HrSyncMapper使用此数据源
 * 
 * 注意：使用@Order(1)确保此配置类的@MapperScan优先级高于主应用类的@MapperScan
 * 这样即使主应用类也扫描了remote包，此配置也会生效
 * 
 * @author system
 * @date 2024
 */
@Configuration
@Order(1) // 确保此配置类的优先级高于主应用类的@MapperScan
@MapperScan(
    basePackages = "com.microport.healthcert.mapper.remote",
    sqlSessionFactoryRef = "remoteSqlSessionFactory",
    sqlSessionTemplateRef = "remoteSqlSessionTemplate"
)
public class RemoteDataSourceConfig {

    /**
     * 创建远程数据源Bean
     * 连接远程proposal_improvement数据库
     * 配置为只读模式
     * 
     * @return 远程数据源
     */
    @Bean(name = "remoteDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.remote")
    public DataSource remoteDataSource() {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .url("jdbc:mysql://10.11.100.202:3306/proposal_improvement?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai")
                .username("root")
                .password("Mtdb@123")
                .build();
        
        // 设置数据源为只读模式（如果使用HikariCP连接池）
        if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
            ((com.zaxxer.hikari.HikariDataSource) dataSource).setReadOnly(true);
        }
        
        return dataSource;
    }

    /**
     * 创建远程数据源的SqlSessionFactory
     * 
     * @param dataSource 远程数据源
     * @return SqlSessionFactory
     * @throws Exception 配置异常
     */
    @Bean(name = "remoteSqlSessionFactory")
    public SqlSessionFactory remoteSqlSessionFactory(@Qualifier("remoteDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        // 设置数据源
        sessionFactory.setDataSource(dataSource);
        // 设置Mapper XML文件位置（如果存在，否则使用注解方式）
        try {
            org.springframework.core.io.Resource[] resources = 
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/remote/*.xml");
            // 只有当找到XML文件时才设置
            if (resources != null && resources.length > 0) {
                sessionFactory.setMapperLocations(resources);
            }
        } catch (java.io.FileNotFoundException e) {
            // 如果mapper/remote目录不存在，使用注解方式，不设置XML位置
            // 这是正常情况，因为MyBatis-Plus支持注解方式
        }
        // 设置实体类包路径
        sessionFactory.setTypeAliasesPackage("com.microport.healthcert.entity.remote");
        // 创建SqlSessionFactory
        return sessionFactory.getObject();
    }

    /**
     * 创建远程数据源的SqlSessionTemplate
     * 
     * @param sqlSessionFactory 远程数据源的SqlSessionFactory
     * @return SqlSessionTemplate
     */
    @Bean(name = "remoteSqlSessionTemplate")
    public SqlSessionTemplate remoteSqlSessionTemplate(@Qualifier("remoteSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 创建远程数据源的事务管理器
     * 
     * @param dataSource 远程数据源
     * @return 事务管理器
     */
    @Bean(name = "remoteTransactionManager")
    public DataSourceTransactionManager remoteTransactionManager(@Qualifier("remoteDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

