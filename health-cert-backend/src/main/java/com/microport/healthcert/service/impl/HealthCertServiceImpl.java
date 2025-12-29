package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.dto.HealthCertDTO;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.HealthCertificateMapper;
import com.microport.healthcert.mapper.OperationLogMapper;
import com.microport.healthcert.service.HealthCertService;
import com.microport.healthcert.service.OcrService;
import com.microport.healthcert.util.FileUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康证服务实现类
 * 
 * @author system
 * @date 2024
 */
@Service
public class HealthCertServiceImpl implements HealthCertService {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private HealthCertificateMapper healthCertificateMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 上传目录
     */
    private static final String UPLOAD_DIR = "uploads";

    /**
     * 上传图片并调用OCR识别
     * 
     * @param file 图片文件
     * @return 包含图片路径和OCR结果的DTO
     */
    @Override
    public HealthCertDTO uploadAndOcr(MultipartFile file) {
        try {
            // 验证图片
            FileUtil.validateImage(file);

            // 保存文件
            String relativePath = FileUtil.saveFile(file, UPLOAD_DIR);

            // 创建临时文件用于OCR识别
            File tempFile = File.createTempFile("ocr_", file.getOriginalFilename());
            file.transferTo(tempFile);

            // 调用OCR识别
            HealthCertDTO ocrResult = ocrService.callOcrApi(tempFile);

            // 删除临时文件
            tempFile.delete();

            // 设置图片路径
            ocrResult.setImagePath(relativePath);

            return ocrResult;
        } catch (Exception e) {
            // 上传或OCR失败，返回空结果，允许手动填写
            HealthCertDTO dto = new HealthCertDTO();
            try {
                // 如果文件已保存，设置图片路径
                String relativePath = FileUtil.saveFile(file, UPLOAD_DIR);
                dto.setImagePath(relativePath);
            } catch (Exception ex) {
                // 文件保存也失败，返回空对象
            }
            return dto;
        }
    }

    /**
     * 提交健康证
     * 验证编号唯一性，验证所有必填字段，保存健康证记录(状态为pending)
     * 如果健康证编号已存在且状态为rejected，则允许重新提交（更新记录）
     * 
     * @param dto 健康证数据
     * @param userId 用户ID
     * @param username 用户名
     */
    @Override
    public void submitHealthCert(HealthCertDTO dto, Long userId, String username) {
        // 验证必填字段
        validateRequiredFields(dto);

        // 获取员工信息
        Employee employee = employeeMapper.selectById(userId);
        if (employee == null) {
            throw new RuntimeException("员工信息不存在");
        }

        // 检查健康证编号是否已存在且is_current=1（保证生效记录的唯一性）
        LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthCertificate::getCertNumber, dto.getCertNumber())
               .eq(HealthCertificate::getIsCurrent, 1);
        HealthCertificate existingCurrentCert = healthCertificateMapper.selectOne(wrapper);

        // 如果编号已存在且is_current=1，先将旧的设为is_current=0（保证生效记录唯一性）
        if (existingCurrentCert != null) {
            existingCurrentCert.setIsCurrent(0);
            existingCurrentCert.setUpdatedAt(LocalDateTime.now());
            healthCertificateMapper.updateById(existingCurrentCert);
        }

        // 检查是否有相同编号且状态为rejected的记录（允许重新提交）
        LambdaQueryWrapper<HealthCertificate> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(HealthCertificate::getCertNumber, dto.getCertNumber())
                      .eq(HealthCertificate::getStatus, "rejected")
                      .orderByDesc(HealthCertificate::getCreatedAt)
                      .last("LIMIT 1");
        HealthCertificate rejectedCert = healthCertificateMapper.selectOne(rejectedWrapper);

        if (rejectedCert != null) {
            // 如果存在已拒绝的记录，更新该记录（重新提交）
            BeanUtils.copyProperties(dto, rejectedCert);
            rejectedCert.setEmployeeId(userId);
            rejectedCert.setSfUserId(employee.getSfUserId());
            rejectedCert.setEmployeeName(employee.getName());
            rejectedCert.setStatus("pending"); // 重新设置为待审核状态
            rejectedCert.setSubmitTime(LocalDateTime.now());
            rejectedCert.setRejectReason(null); // 清空拒绝原因
            rejectedCert.setAuditTime(null); // 清空审核时间
            rejectedCert.setAuditorId(null); // 清空审核人ID
            rejectedCert.setAuditorName(null); // 清空审核人姓名
            rejectedCert.setIsCurrent(0); // 待审核状态时设为0，审核通过后再设为1
            rejectedCert.setVersion(rejectedCert.getVersion() + 1); // 版本号递增
            rejectedCert.setUpdatedAt(LocalDateTime.now());

            // 更新健康证记录
            healthCertificateMapper.updateById(rejectedCert);

            // 记录操作日志
            saveOperationLog(userId, username, "employee", "resubmit", "重新提交健康证，编号：" + dto.getCertNumber());
        } else {
            // 如果不存在已拒绝的记录，创建新记录
            HealthCertificate healthCert = new HealthCertificate();
            BeanUtils.copyProperties(dto, healthCert);
            healthCert.setEmployeeId(userId);
            healthCert.setSfUserId(employee.getSfUserId());
            healthCert.setEmployeeName(employee.getName());
            healthCert.setStatus("pending"); // 状态为待审核
            healthCert.setSubmitTime(LocalDateTime.now());
            healthCert.setIsCurrent(0); // 待审核状态时设为0，审核通过后再设为1
            healthCert.setVersion(1);

            // 保存健康证记录
            healthCertificateMapper.insert(healthCert);

            // 记录操作日志
            saveOperationLog(userId, username, "employee", "submit", "提交健康证，编号：" + dto.getCertNumber());
        }
    }

    /**
     * 查询我的健康证列表
     * 
     * @param userId 用户ID
     * @return 健康证列表
     */
    @Override
    public List<HealthCertificate> getMyHealthCertList(Long userId) {
        LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthCertificate::getEmployeeId, userId)
               .orderByDesc(HealthCertificate::getCreatedAt);
        return healthCertificateMapper.selectList(wrapper);
    }

    /**
     * 根据ID查询健康证详情（员工端）
     * 只能查询自己的健康证
     * 
     * @param certId 健康证ID
     * @param userId 用户ID（用于验证权限）
     * @return 健康证详情
     */
    @Override
    public HealthCertificate getMyHealthCertById(Long certId, Long userId) {
        HealthCertificate cert = healthCertificateMapper.selectById(certId);
        if (cert == null) {
            throw new IllegalArgumentException("健康证不存在");
        }
        // 验证是否属于当前用户
        if (!cert.getEmployeeId().equals(userId)) {
            throw new IllegalArgumentException("无权访问该健康证");
        }
        return cert;
    }

    /**
     * 验证必填字段
     */
    private void validateRequiredFields(HealthCertDTO dto) {
        if (dto.getCertNumber() == null || dto.getCertNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("健康证编号不能为空");
        }
        if (dto.getEmployeeName() == null || dto.getEmployeeName().trim().isEmpty()) {
            throw new IllegalArgumentException("员工姓名不能为空");
        }
        if (dto.getIssueDate() == null) {
            throw new IllegalArgumentException("发证日期不能为空");
        }
        if (dto.getExpiryDate() == null) {
            throw new IllegalArgumentException("有效期至不能为空");
        }
        if (dto.getImagePath() == null || dto.getImagePath().trim().isEmpty()) {
            throw new IllegalArgumentException("健康证图片不能为空");
        }
    }


    /**
     * 保存操作日志
     */
    private void saveOperationLog(Long userId, String userName, String userType, String operation, String description) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setUserType(userType);
        log.setOperation(operation);
        log.setDescription(description);
        log.setResult("success");
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}

