package com.microport.healthcert.entity.remote;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 远程HR同步员工信息实体类
 * 对应远程数据库表: hr_sync (proposal_improvement数据库)
 * 用于从远程数据库同步员工数据到本地employees表
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("hr_sync")
public class HrSync {

    /**
     * 主键ID
     * 注意：远程hr_sync表可能没有id字段，此字段仅用于兼容，不会从数据库读取
     */
    @TableField(exist = false)
    private Long id;

    /**
     * 员工域账号
     */
    @TableField("sf_user_id")
    private String sfUserId;

    /**
     * 工号
     */
    @TableField("mp_number")
    private String mpNumber;

    /**
     * 明文密码
     * 远程hr_sync表中的字段名是pwd，本地employees表中的字段名是password
     */
    @TableField("pwd")
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
     * 是否一线员工
     * 远程hr_sync表中是字符类型：'Y'表示是，'N'表示否
     * 本地employees表中是tinyint类型：1表示是，0表示否
     */
    @TableField("is_frontline_worker")
    private String isFrontlineWorker;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     * 注意：远程hr_sync表没有mobile字段，此字段仅用于兼容，不会从数据库读取
     * 本地employees表会保存员工手机号，但需要从其他渠道获取（如钉钉等）
     */
    @TableField(exist = false)
    private String mobile;
}

