package com.microport.healthcert.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 本地数据源配置类
 * 明确指定本地Mapper使用本地数据源（默认数据源）
 * 
 * 明确列出每个本地Mapper的完整类名，避免扫描remote子包
 * 明确指定使用本地数据源的SqlSessionFactory
 * 
 * 注意：
 * 1. 使用basePackageClasses明确指定每个本地Mapper，避免扫描remote子包
 * 2. 明确指定sqlSessionFactoryRef为本地数据源的SqlSessionFactory
 * 3. RemoteDataSourceConfig的@MapperScan会明确扫描remote包，使用远程数据源
 * 4. 这样确保本地Mapper使用本地数据源，remote Mapper使用远程数据源
 * 
 * @author system
 * @date 2024
 */
@Configuration
@Order(2) // 优先级低于RemoteDataSourceConfig（@Order(1)），确保remote包的配置由RemoteDataSourceConfig处理
@MapperScan(
    basePackageClasses = {
        com.microport.healthcert.mapper.AdminMapper.class,
        com.microport.healthcert.mapper.AuditLockMapper.class,
        com.microport.healthcert.mapper.DepartmentMapper.class,
        com.microport.healthcert.mapper.EmployeeMapper.class,
        com.microport.healthcert.mapper.HealthCertificateMapper.class,
        com.microport.healthcert.mapper.OperationLogMapper.class,
        com.microport.healthcert.mapper.SystemConfigMapper.class
    },
    // 明确指定使用本地数据源的SqlSessionFactory
    sqlSessionFactoryRef = "localSqlSessionFactory"
)
public class LocalDataSourceConfig {

    /**
     * 创建本地数据源Bean
     * 从application.yml或环境变量读取配置
     * 
     * @param env Spring Environment，用于读取配置
     * @return 本地数据源
     */
    @Bean(name = "dataSource")
    @Primary // 标记为主要数据源
    public DataSource localDataSource(Environment env) {
        // 从环境变量或配置文件中读取数据源配置
        // 环境变量的优先级高于配置文件
        // 注意：字符集使用UTF-8（大写），确保中文正确显示
        String url = env.getProperty("spring.datasource.url", 
            "jdbc:mysql://mysql:3306/health_cert_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
        String username = env.getProperty("spring.datasource.username", "root");
        String password = env.getProperty("spring.datasource.password", "root123");
        String driverClassName = env.getProperty("spring.datasource.driver-class-name", 
            "com.mysql.cj.jdbc.Driver");
        
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    /**
     * 创建本地数据源的SqlSessionFactory
     * 使用本地数据源（通过@Qualifier明确指定）
     * 
     * @param dataSource 本地数据源
     * @param mybatisPlusInterceptor MyBatis-Plus拦截器（包含分页插件）
     * @return SqlSessionFactory
     * @throws Exception 配置异常
     */
    @Bean(name = "localSqlSessionFactory")
    @Primary // 标记为主要的SqlSessionFactory
    public SqlSessionFactory localSqlSessionFactory(
            @org.springframework.beans.factory.annotation.Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        // 设置数据源（使用Spring Boot自动配置的默认数据源）
        sessionFactory.setDataSource(dataSource);
        // 设置Mapper XML文件位置
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml")
        );
        // 设置实体类包路径
        sessionFactory.setTypeAliasesPackage("com.microport.healthcert.entity");
        
        // 设置MyBatis-Plus拦截器（包含分页插件）
        if (mybatisPlusInterceptor != null) {
            sessionFactory.setPlugins(mybatisPlusInterceptor);
        } else {
            // 如果拦截器未注入，手动创建分页插件
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
            paginationInnerInterceptor.setMaxLimit(1000L);
            paginationInnerInterceptor.setOverflow(false);
            interceptor.addInnerInterceptor(paginationInnerInterceptor);
            sessionFactory.setPlugins(interceptor);
        }
        
        // 创建SqlSessionFactory
        return sessionFactory.getObject();
    }

    /**
     * 创建本地数据源的事务管理器
     * 使用本地数据源（通过@Qualifier明确指定）
     * 
     * @param dataSource 本地数据源
     * @return 事务管理器
     */
    @Bean(name = "transactionManager")
    @Primary // 标记为主要事务管理器
    public DataSourceTransactionManager transactionManager(@org.springframework.beans.factory.annotation.Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}

