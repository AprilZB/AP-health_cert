-- ============================================
-- 修复中文乱码问题
-- 此脚本用于修复已存在数据的中文乱码问题
-- ============================================

-- 1. 确保数据库使用utf8mb4字符集
ALTER DATABASE health_cert_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 修复admins表中的中文数据
UPDATE `admins` SET `real_name` = '超级管理员' WHERE `username` = 'admin';

-- 3. 修复departments表中的中文数据
UPDATE `departments` SET `dept_name` = '浙江脉通智造科技(集团)有限公司' WHERE `dept_level` = 1;

-- 4. 修复system_configs表中的中文数据
UPDATE `system_configs` SET `description` = 'SMTP服务器地址' WHERE `config_key` = 'email.smtp.host';
UPDATE `system_configs` SET `description` = 'SMTP端口' WHERE `config_key` = 'email.smtp.port';
UPDATE `system_configs` SET `description` = '发件人邮箱' WHERE `config_key` = 'email.from';
UPDATE `system_configs` SET `description` = 'SMTP用户名' WHERE `config_key` = 'email.username';
UPDATE `system_configs` SET `description` = 'SMTP密码' WHERE `config_key` = 'email.password';
UPDATE `system_configs` SET `description` = '邮件主题模板' WHERE `config_key` = 'email.subject.template';
UPDATE `system_configs` SET `description` = '邮件内容模板' WHERE `config_key` = 'email.content.template';
UPDATE `system_configs` SET `description` = '钉钉企业ID' WHERE `config_key` = 'dingtalk.corp_id';
UPDATE `system_configs` SET `description` = '钉钉应用AppKey' WHERE `config_key` = 'dingtalk.app_key';
UPDATE `system_configs` SET `description` = '钉钉应用AppSecret' WHERE `config_key` = 'dingtalk.app_secret';
UPDATE `system_configs` SET `description` = '提醒天数(逗号分隔)' WHERE `config_key` = 'reminder.days';
UPDATE `system_configs` SET `description` = '提醒时间(HH:mm)' WHERE `config_key` = 'reminder.time';
UPDATE `system_configs` SET `description` = '已过期提醒频率(天)' WHERE `config_key` = 'reminder.expired.frequency';
UPDATE `system_configs` SET `description` = 'OCR服务地址' WHERE `config_key` = 'ocr.service.url';
UPDATE `system_configs` SET `description` = '员工同步时间点' WHERE `config_key` = 'sync.times';
UPDATE `system_configs` SET `description` = '数据备份时间' WHERE `config_key` = 'backup.time';

-- 5. 修复system_configs表中的中文配置值
UPDATE `system_configs` SET `config_value` = '【健康证提醒】您的健康证即将到期' WHERE `config_key` = 'email.subject.template';
UPDATE `system_configs` SET `config_value` = '尊敬的{name}:\n\n您的健康证(编号:{certNumber})将于{expiryDate}到期,请及时更新。\n\n健康证管理系统' WHERE `config_key` = 'email.content.template';

-- 6. 修复reminder_rules表中的中文数据
UPDATE `reminder_rules` SET `rule_name` = '到期前30天提醒' WHERE `rule_type` = 'expiring' AND `trigger_days` = 30;
UPDATE `reminder_rules` SET `rule_name` = '到期前15天提醒' WHERE `rule_type` = 'expiring' AND `trigger_days` = 15;
UPDATE `reminder_rules` SET `rule_name` = '到期前7天提醒' WHERE `rule_type` = 'expiring' AND `trigger_days` = 7;
UPDATE `reminder_rules` SET `rule_name` = '到期前3天提醒' WHERE `rule_type` = 'expiring' AND `trigger_days` = 3;
UPDATE `reminder_rules` SET `rule_name` = '到期前1天提醒' WHERE `rule_type` = 'expiring' AND `trigger_days` = 1;
UPDATE `reminder_rules` SET `rule_name` = '已过期提醒(每周)' WHERE `rule_type` = 'expired' AND `trigger_days` = 0;

