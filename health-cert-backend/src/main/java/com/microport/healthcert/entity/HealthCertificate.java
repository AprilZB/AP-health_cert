package com.microport.healthcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康证信息实体类
 * 对应数据库表: health_certificates
 * 
 * @author system
 * @date 2024
 */
@Data
@TableName("health_certificates")
public class HealthCertificate {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 健康证编号(全局唯一)
     */
    @TableField("cert_number")
    private String certNumber;

    /**
     * 员工ID(关联employees.id)
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 员工域账号(冗余字段)
     */
    @TableField("sf_user_id")
    private String sfUserId;

    /**
     * 员工姓名
     */
    @TableField("employee_name")
    private String employeeName;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 健康证类别
     */
    @TableField("category")
    private String category;

    /**
     * 发证日期
     */
    @TableField("issue_date")
    private LocalDate issueDate;

    /**
     * 有效期至
     */
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /**
     * 发证机构
     */
    @TableField("issuing_authority")
    private String issuingAuthority;

    /**
     * 健康证图片路径
     */
    @TableField("image_path")
    private String imagePath;

    /**
     * OCR原始识别结果(JSON)
     */
    @TableField("ocr_raw_data")
    private String ocrRawData;

    /**
     * 状态: draft草稿/pending待审核/approved已通过/rejected已拒绝/expired已过期
     */
    @TableField("status")
    private String status;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @TableField("auditor_name")
    private String auditorName;

    /**
     * 拒绝原因
     */
    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 是否当前有效健康证(0否1是)
     */
    @TableField("is_current")
    private Integer isCurrent;

    /**
     * 版本号(历史版本递增)
     */
    @TableField("version")
    private Integer version;

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

