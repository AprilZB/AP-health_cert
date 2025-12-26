package com.microport.healthcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表: operation_logs
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("operation_logs")
public class OperationLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户类型: employee员工/admin管理员
     */
    @TableField("user_type")
    private String userType;

    /**
     * 操作类型: login登录/logout登出/upload上传/submit提交/audit审核/download导出/config配置/password修改密码
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作结果: success成功/fail失败
     */
    @TableField("result")
    private String result;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 执行时长(毫秒)
     */
    @TableField("execution_time")
    private Integer executionTime;

    /**
     * 操作时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

