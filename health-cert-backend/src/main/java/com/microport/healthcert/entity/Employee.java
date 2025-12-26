package com.microport.healthcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工信息实体类
 * 对应数据库表: employees
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("employees")
public class Employee {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工域账号(远程库同步)
     */
    @TableField("sf_user_id")
    private String sfUserId;

    /**
     * 工号
     */
    @TableField("mp_number")
    private String mpNumber;

    /**
     * 明文密码(远程库同步)
     */
    @TableField("password")
    private String password;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 部门名称
     */
    @TableField("depart_name_cn")
    private String departNameCn;

    /**
     * 上级部门
     */
    @TableField("sup_dep")
    private String supDep;

    /**
     * 上级域账号
     */
    @TableField("supervisor_sf_user_id")
    private String supervisorSfUserId;

    /**
     * 职位
     */
    @TableField("job_name_cn")
    private String jobNameCn;

    /**
     * 岗位
     */
    @TableField("position_name_cn")
    private String positionNameCn;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 是否一线员工(0否1是)
     */
    @TableField("is_frontline_worker")
    private Integer isFrontlineWorker;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 手机号(用于匹配钉钉userid)
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 钉钉用户ID
     */
    @TableField("dingtalk_userid")
    private String dingtalkUserid;

    /**
     * 在职状态(0离职1在职)
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 最后同步时间
     */
    @TableField("sync_time")
    private LocalDateTime syncTime;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

