package com.microport.healthcert.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康证数据传输对象
 * 用于接收OCR识别结果和提交健康证数据
 * 
 * @author system
 * @date 2024
 */
@Data
public class HealthCertDTO {

    /**
     * 健康证编号
     */
    private String certNumber;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 健康证类别
     */
    private String category;

    /**
     * 发证日期
     */
    private LocalDate issueDate;

    /**
     * 有效期至
     */
    private LocalDate expiryDate;

    /**
     * 发证机构
     */
    private String issuingAuthority;

    /**
     * 健康证图片路径
     */
    private String imagePath;
}

