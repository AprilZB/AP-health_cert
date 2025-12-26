package com.microport.healthcert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microport.healthcert.entity.Admin;
import com.microport.healthcert.entity.Employee;
import com.microport.healthcert.entity.HealthCertificate;
import com.microport.healthcert.entity.SystemConfig;
import com.microport.healthcert.mapper.AdminMapper;
import com.microport.healthcert.mapper.EmployeeMapper;
import com.microport.healthcert.mapper.SystemConfigMapper;
import com.microport.healthcert.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 * 邮件服务实现类
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 发送邮件
     * 使用Spring的JavaMailSender，SMTP配置从system_configs表读取
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            // 从system_configs表读取SMTP配置
            JavaMailSender mailSender = createMailSender();
            if (mailSender == null) {
                log.warn("邮件配置不完整，无法发送邮件");
                return;
            }

            // 创建邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getConfigValue("email.from"));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            // 发送邮件
            mailSender.send(message);
            log.info("邮件发送成功：收件人={}, 主题={}", to, subject);

        } catch (Exception e) {
            // 发送失败记录日志，不抛异常
            log.error("邮件发送失败：收件人={}, 主题={}, 错误={}", to, subject, e.getMessage(), e);
        }
    }

    /**
     * 发送健康证到期提醒邮件
     * 从system_configs读取邮件模板，替换模板变量
     * 如果员工无邮箱，发给主管；主管也无邮箱，发给系统管理员
     * 
     * @param employee 员工信息
     * @param cert 健康证信息
     */
    @Override
    public void sendReminderEmail(Employee employee, HealthCertificate cert) {
        try {
            // 从system_configs读取邮件模板
            String subjectTemplate = getConfigValue("email.subject.template");
            String contentTemplate = getConfigValue("email.content.template");

            if (subjectTemplate == null || contentTemplate == null) {
                log.warn("邮件模板未配置，无法发送提醒邮件");
                return;
            }

            // 替换模板变量
            String subject = replaceTemplateVariables(subjectTemplate, employee, cert);
            String content = replaceTemplateVariables(contentTemplate, employee, cert);

            // 确定收件人
            String recipientEmail = determineRecipient(employee);

            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("无法确定收件人邮箱，员工={}", employee.getSfUserId());
                return;
            }

            // 发送邮件
            sendEmail(recipientEmail, subject, content);

        } catch (Exception e) {
            // 发送失败记录日志，不抛异常
            log.error("发送健康证提醒邮件失败：员工={}, 健康证编号={}, 错误={}",
                    employee.getSfUserId(), cert.getCertNumber(), e.getMessage(), e);
        }
    }

    /**
     * 创建JavaMailSender
     * 从system_configs表读取SMTP配置
     * 
     * @return JavaMailSender对象，如果配置不完整返回null
     */
    private JavaMailSender createMailSender() {
        try {
            // 读取SMTP配置
            String host = getConfigValue("email.smtp.host");
            String portStr = getConfigValue("email.smtp.port");
            String username = getConfigValue("email.username");
            String password = getConfigValue("email.password");

            if (host == null || portStr == null || username == null) {
                log.warn("SMTP配置不完整，无法创建邮件发送器");
                return null;
            }

            // 创建JavaMailSenderImpl
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(portStr));
            mailSender.setUsername(username);
            if (password != null && !password.trim().isEmpty()) {
                mailSender.setPassword(password);
            }

            // 配置SMTP属性
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.debug", "false");

            return mailSender;

        } catch (Exception e) {
            log.error("创建邮件发送器失败", e);
            return null;
        }
    }

    /**
     * 从system_configs表读取配置值
     * 
     * @param configKey 配置键
     * @return 配置值，如果不存在返回null
     */
    private String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 替换模板变量
     * 支持变量：{name}, {certNumber}, {expiryDate}
     * 
     * @param template 模板字符串
     * @param employee 员工信息
     * @param cert 健康证信息
     * @return 替换后的字符串
     */
    private String replaceTemplateVariables(String template, Employee employee, HealthCertificate cert) {
        if (template == null) {
            return "";
        }

        String result = template;
        // 替换{name}
        if (employee.getName() != null) {
            result = result.replace("{name}", employee.getName());
        }
        // 替换{certNumber}
        if (cert.getCertNumber() != null) {
            result = result.replace("{certNumber}", cert.getCertNumber());
        }
        // 替换{expiryDate}
        if (cert.getExpiryDate() != null) {
            result = result.replace("{expiryDate}", cert.getExpiryDate().format(DATE_FORMATTER));
        }

        return result;
    }

    /**
     * 确定收件人邮箱
     * 如果员工有邮箱，发给员工
     * 如果员工无邮箱，发给主管
     * 如果主管也无邮箱，发给系统管理员
     * 
     * @param employee 员工信息
     * @return 收件人邮箱
     */
    private String determineRecipient(Employee employee) {
        // 1. 如果员工有邮箱，发给员工
        if (employee.getEmail() != null && !employee.getEmail().trim().isEmpty()) {
            return employee.getEmail();
        }

        // 2. 如果员工无邮箱，发给主管
        if (employee.getSupervisorSfUserId() != null && !employee.getSupervisorSfUserId().trim().isEmpty()) {
            LambdaQueryWrapper<Employee> supervisorWrapper = new LambdaQueryWrapper<>();
            supervisorWrapper.eq(Employee::getSfUserId, employee.getSupervisorSfUserId());
            Employee supervisor = employeeMapper.selectOne(supervisorWrapper);

            if (supervisor != null && supervisor.getEmail() != null && !supervisor.getEmail().trim().isEmpty()) {
                return supervisor.getEmail();
            }
        }

        // 3. 主管也无邮箱，发给系统管理员
        // 查询第一个有邮箱的管理员
        LambdaQueryWrapper<Admin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(Admin::getIsActive, 1)
                    .isNotNull(Admin::getEmail)
                    .ne(Admin::getEmail, "");
        List<Admin> admins = adminMapper.selectList(adminWrapper);

        if (admins != null && !admins.isEmpty()) {
            return admins.get(0).getEmail();
        }

        // 如果都没有邮箱，返回null
        return null;
    }
}

