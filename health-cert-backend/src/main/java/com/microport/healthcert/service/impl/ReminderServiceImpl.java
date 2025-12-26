package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.entity.OperationLog;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.HealthCertificateMapper;
import com.microport.healthcert.mapper.OperationLogMapper;
import com.microport.healthcert.service.DingTalkService;
import com.microport.healthcert.service.EmailService;
import com.microport.healthcert.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提醒服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class ReminderServiceImpl implements ReminderService {

    /**
     * 提醒天数列表（30/15/7/3/1天）
     */
    private static final int[] REMINDER_DAYS = {30, 15, 7, 3, 1};

    @Autowired
    private HealthCertificateMapper healthCertificateMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DingTalkService dingTalkService;

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 发送提醒
     * 查询即将到期的健康证（30/15/7/3/1天）和已过期的健康证（每周提醒一次），调用EmailService和DingTalkService发送
     */
    @Override
    public void sendReminders() {
        log.info("开始执行健康证到期提醒任务");

        try {
            // 1. 查询即将到期的健康证（30/15/7/3/1天）
            LocalDate today = LocalDate.now();
            for (int days : REMINDER_DAYS) {
                LocalDate targetDate = today.plusDays(days);
                sendExpiringReminders(targetDate, days);
            }

            // 2. 查询已过期的健康证（每周提醒一次）
            sendExpiredReminders();

            log.info("健康证到期提醒任务执行完成");

        } catch (Exception e) {
            log.error("健康证到期提醒任务执行失败", e);
        }
    }

    /**
     * 发送即将到期的提醒
     * 
     * @param targetDate 目标日期（今天+N天）
     * @param days 提醒天数
     */
    private void sendExpiringReminders(LocalDate targetDate, int days) {
        try {
            // 查询指定日期到期的健康证
            LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HealthCertificate::getExpiryDate, targetDate)
                   .eq(HealthCertificate::getStatus, "approved") // 只查询已通过的健康证
                   .eq(HealthCertificate::getIsCurrent, 1); // 只查询当前有效的健康证

            List<HealthCertificate> certificates = healthCertificateMapper.selectList(wrapper);

            log.info("查询到{}条将在{}天后到期的健康证", certificates.size(), days);

            for (HealthCertificate cert : certificates) {
                // 获取员工信息
                Employee employee = employeeMapper.selectById(cert.getEmployeeId());
                if (employee == null || employee.getIsActive() == 0) {
                    continue; // 员工不存在或已离职，跳过
                }

                // 发送邮件提醒
                try {
                    emailService.sendReminderEmail(employee, cert);
                } catch (Exception e) {
                    log.error("发送邮件提醒失败，员工：{}，健康证编号：{}", employee.getSfUserId(), cert.getCertNumber(), e);
                }

                // 发送钉钉提醒
                try {
                    sendDingTalkReminder(employee, cert, days, false);
                } catch (Exception e) {
                    log.error("发送钉钉提醒失败，员工：{}，健康证编号：{}", employee.getSfUserId(), cert.getCertNumber(), e);
                }

                // 记录发送日志
                saveReminderLog(employee.getId(), employee.getSfUserId(), "employee", 
                        "reminder", "发送健康证到期前" + days + "天提醒，健康证编号：" + cert.getCertNumber());
            }

        } catch (Exception e) {
            log.error("发送即将到期提醒失败，天数：{}", days, e);
        }
    }

    /**
     * 发送已过期的提醒（每周提醒一次）
     */
    private void sendExpiredReminders() {
        try {
            LocalDate today = LocalDate.now();

            // 查询已过期的健康证
            LambdaQueryWrapper<HealthCertificate> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(HealthCertificate::getExpiryDate, today)
                   .eq(HealthCertificate::getStatus, "approved") // 只查询已通过的健康证
                   .eq(HealthCertificate::getIsCurrent, 1); // 只查询当前有效的健康证

            List<HealthCertificate> certificates = healthCertificateMapper.selectList(wrapper);

            log.info("查询到{}条已过期的健康证", certificates.size());

            for (HealthCertificate cert : certificates) {
                // 获取员工信息
                Employee employee = employeeMapper.selectById(cert.getEmployeeId());
                if (employee == null || employee.getIsActive() == 0) {
                    continue; // 员工不存在或已离职，跳过
                }

                // 发送邮件提醒
                try {
                    emailService.sendReminderEmail(employee, cert);
                } catch (Exception e) {
                    log.error("发送已过期邮件提醒失败，员工：{}，健康证编号：{}", employee.getSfUserId(), cert.getCertNumber(), e);
                }

                // 发送钉钉提醒
                try {
                    sendDingTalkReminder(employee, cert, 0, true);
                } catch (Exception e) {
                    log.error("发送已过期钉钉提醒失败，员工：{}，健康证编号：{}", employee.getSfUserId(), cert.getCertNumber(), e);
                }

                // 记录发送日志
                saveReminderLog(employee.getId(), employee.getSfUserId(), "employee", 
                        "reminder", "发送健康证已过期提醒，健康证编号：" + cert.getCertNumber());
            }

        } catch (Exception e) {
            log.error("发送已过期提醒失败", e);
        }
    }

    /**
     * 发送钉钉提醒
     * 
     * @param employee 员工信息
     * @param cert 健康证信息
     * @param days 到期天数（0表示已过期）
     * @param isExpired 是否已过期
     */
    private void sendDingTalkReminder(Employee employee, HealthCertificate cert, int days, boolean isExpired) {
        // 如果员工没有手机号，无法发送钉钉消息
        if (employee.getMobile() == null || employee.getMobile().trim().isEmpty()) {
            return;
        }

        // 根据手机号获取钉钉userid
        String userId = dingTalkService.getUserIdByMobile(employee.getMobile());
        if (userId == null) {
            log.warn("无法获取钉钉userid，手机号：{}", employee.getMobile());
            return;
        }

        // 构建markdown格式的消息内容
        StringBuilder content = new StringBuilder();
        content.append("## 健康证提醒\n\n");
        content.append("**姓名：** ").append(employee.getName()).append("\n\n");
        content.append("**健康证编号：** ").append(cert.getCertNumber()).append("\n\n");
        content.append("**有效期至：** ").append(cert.getExpiryDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");

        if (isExpired) {
            content.append("**提醒内容：** 您的健康证已过期，请及时更新！\n\n");
        } else {
            content.append("**提醒内容：** 您的健康证将在").append(days).append("天后到期，请及时更新！\n\n");
        }

        // 发送钉钉工作通知
        dingTalkService.sendWorkMessage(userId, content.toString());
    }

    /**
     * 保存提醒日志
     * 
     * @param userId 用户ID
     * @param userName 用户名
     * @param userType 用户类型
     * @param operation 操作类型
     * @param description 操作描述
     */
    private void saveReminderLog(Long userId, String userName, String userType, String operation, String description) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUserName(userName);
            log.setUserType(userType);
            log.setOperation(operation);
            log.setDescription(description);
            log.setResult("success");
            log.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception e) {
            log.error("保存提醒日志失败", e);
        }
    }
}

