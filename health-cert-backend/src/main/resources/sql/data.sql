-- ============================================
-- 员工健康证管理系统 - 初始数据
-- ============================================

-- 初始化根部门
INSERT INTO `departments` (`dept_name`, `parent_dept_name`, `dept_level`, `dept_path`) 
VALUES ('浙江脉通智造科技(集团)有限公司', NULL, 1, '/浙江脉通智造科技(集团)有限公司/');

-- 初始化超级管理员
INSERT INTO `admins` (`username`, `password`, `real_name`, `email`, `role`) 
VALUES ('admin', 'admin123', '超级管理员', 'APsysadmin@accupathmed.com', 'super_admin');

-- 初始化系统配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `group_name`) VALUES
('email.smtp.host', 'mail.microport.com', 'string', 'SMTP服务器地址', 'email'),
('email.smtp.port', '25', 'int', 'SMTP端口', 'email'),
('email.from', 'APsysadmin@accupathmed.com', 'string', '发件人邮箱', 'email'),
('email.username', 'APsysadmin@accupathmed.com', 'string', 'SMTP用户名', 'email'),
('email.password', '', 'string', 'SMTP密码', 'email'),
('email.subject.template', '【健康证提醒】您的健康证即将到期', 'string', '邮件主题模板', 'email'),
('email.content.template', '尊敬的{name}:\n\n您的健康证(编号:{certNumber})将于{expiryDate}到期,请及时更新。\n\n健康证管理系统', 'string', '邮件内容模板', 'email'),
('dingtalk.corp_id', '', 'string', '钉钉企业ID', 'dingtalk'),
('dingtalk.app_key', '', 'string', '钉钉应用AppKey', 'dingtalk'),
('dingtalk.app_secret', '', 'string', '钉钉应用AppSecret', 'dingtalk'),
('reminder.days', '30,15,7,3,1', 'string', '提醒天数(逗号分隔)', 'reminder'),
('reminder.time', '09:00', 'string', '提醒时间(HH:mm)', 'reminder'),
('reminder.expired.frequency', '7', 'int', '已过期提醒频率(天)', 'reminder'),
('ocr.service.url', 'http://10.11.100.238:8081/predict', 'string', 'OCR服务地址', 'system'),
('sync.times', '00:00,12:00,18:00', 'string', '员工同步时间点', 'system'),
('backup.time', '02:00', 'string', '数据备份时间', 'system');

-- 初始化提醒规则
INSERT INTO `reminder_rules` (`rule_name`, `rule_type`, `trigger_days`, `trigger_time`, `is_active`) VALUES
('到期前30天提醒', 'expiring', 30, '09:00:00', 1),
('到期前15天提醒', 'expiring', 15, '09:00:00', 1),
('到期前7天提醒', 'expiring', 7, '09:00:00', 1),
('到期前3天提醒', 'expiring', 3, '09:00:00', 1),
('到期前1天提醒', 'expiring', 1, '09:00:00', 1),
('已过期提醒(每周)', 'expired', 0, '09:00:00', 1);
