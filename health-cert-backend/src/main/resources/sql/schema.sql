-- ============================================
-- 员工健康证管理系统 - 数据库表结构定义
-- 字符集: utf8mb4
-- 引擎: InnoDB
-- ============================================

-- 1. 员工表 (employees)
CREATE TABLE `employees` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sf_user_id` VARCHAR(50) NOT NULL COMMENT '员工域账号(远程库同步)',
  `mp_number` VARCHAR(50) DEFAULT NULL COMMENT '工号',
  `password` VARCHAR(100) NOT NULL COMMENT '明文密码(远程库同步)',
  `name` VARCHAR(100) NOT NULL COMMENT '姓名',
  `depart_name_cn` VARCHAR(200) DEFAULT NULL COMMENT '部门名称',
  `sup_dep` VARCHAR(200) DEFAULT NULL COMMENT '上级部门',
  `supervisor_sf_user_id` VARCHAR(50) DEFAULT NULL COMMENT '上级域账号',
  `job_name_cn` VARCHAR(100) DEFAULT NULL COMMENT '职位',
  `position_name_cn` VARCHAR(100) DEFAULT NULL COMMENT '岗位',
  `role` VARCHAR(50) DEFAULT NULL COMMENT '角色',
  `is_frontline_worker` TINYINT(1) DEFAULT 0 COMMENT '是否一线员工(0否1是)',
  `email` VARCHAR(200) DEFAULT NULL COMMENT '邮箱地址',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号(用于匹配钉钉userid)',
  `dingtalk_userid` VARCHAR(100) DEFAULT NULL COMMENT '钉钉用户ID',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '在职状态(0离职1在职)',
  `sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sf_user_id` (`sf_user_id`),
  KEY `idx_mp_number` (`mp_number`),
  KEY `idx_email` (`email`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_supervisor` (`supervisor_sf_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- 2. 健康证表 (health_certificates)
CREATE TABLE `health_certificates` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cert_number` VARCHAR(100) NOT NULL COMMENT '健康证编号(允许重复，但is_current=1时唯一)',
  `employee_id` BIGINT(20) NOT NULL COMMENT '员工ID(关联employees.id)',
  `sf_user_id` VARCHAR(50) NOT NULL COMMENT '员工域账号(冗余字段)',
  `employee_name` VARCHAR(100) NOT NULL COMMENT '员工姓名',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `age` INT(3) DEFAULT NULL COMMENT '年龄',
  `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
  `category` VARCHAR(100) DEFAULT NULL COMMENT '健康证类别',
  `issue_date` DATE NOT NULL COMMENT '发证日期',
  `expiry_date` DATE NOT NULL COMMENT '有效期至',
  `issuing_authority` VARCHAR(200) DEFAULT NULL COMMENT '发证机构',
  `image_path` VARCHAR(500) NOT NULL COMMENT '健康证图片路径',
  `ocr_raw_data` TEXT DEFAULT NULL COMMENT 'OCR原始识别结果(JSON)',
  `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态: draft草稿/pending待审核/approved已通过/rejected已拒绝/expired已过期',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `auditor_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` VARCHAR(100) DEFAULT NULL COMMENT '审核人姓名',
  `reject_reason` TEXT DEFAULT NULL COMMENT '拒绝原因',
  `is_current` TINYINT(1) DEFAULT 1 COMMENT '是否当前有效健康证(0否1是)',
  `version` INT(5) DEFAULT 1 COMMENT '版本号(历史版本递增)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cert_number` (`cert_number`),
  KEY `idx_cert_number_is_current` (`cert_number`, `is_current`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_sf_user_id` (`sf_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_is_current` (`is_current`),
  KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康证信息表';

-- 3. 部门表 (departments)
CREATE TABLE `departments` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_name` VARCHAR(200) NOT NULL COMMENT '部门名称',
  `parent_dept_name` VARCHAR(200) DEFAULT NULL COMMENT '上级部门名称',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '上级部门ID',
  `dept_level` INT(3) DEFAULT 1 COMMENT '部门层级(1根部门)',
  `dept_path` VARCHAR(1000) DEFAULT NULL COMMENT '部门路径(用于树形查询)',
  `employee_count` INT(10) DEFAULT 0 COMMENT '部门人数(统计用)',
  `sort_order` INT(5) DEFAULT 0 COMMENT '排序序号',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_name` (`dept_name`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 4. 管理员表 (admins)
CREATE TABLE `admins` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '管理员账号',
  `password` VARCHAR(100) NOT NULL COMMENT '明文密码',
  `real_name` VARCHAR(100) NOT NULL COMMENT '真实姓名',
  `email` VARCHAR(200) NOT NULL COMMENT '邮箱地址(必填)',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) DEFAULT 'admin' COMMENT '角色: super_admin超级管理员/admin普通管理员',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 5. 操作日志表 (operation_logs)
CREATE TABLE `operation_logs` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '操作用户ID',
  `user_name` VARCHAR(100) DEFAULT NULL COMMENT '操作用户名',
  `user_type` VARCHAR(20) DEFAULT NULL COMMENT '用户类型: employee员工/admin管理员',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型: login登录/logout登出/upload上传/submit提交/audit审核/download导出/config配置/password修改密码',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
  `description` TEXT DEFAULT NULL COMMENT '操作描述',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `result` VARCHAR(20) DEFAULT 'success' COMMENT '操作结果: success成功/fail失败',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `execution_time` INT(10) DEFAULT NULL COMMENT '执行时长(毫秒)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 6. 系统配置表 (system_configs)
CREATE TABLE `system_configs` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型: string/int/json',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `group_name` VARCHAR(50) DEFAULT 'system' COMMENT '配置分组: system系统/email邮件/dingtalk钉钉/reminder提醒',
  `is_encrypted` TINYINT(1) DEFAULT 0 COMMENT '是否加密存储',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 7. 提醒规则表 (reminder_rules)
CREATE TABLE `reminder_rules` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型: expiring即将到期/expired已过期',
  `trigger_days` INT(5) DEFAULT NULL COMMENT '触发天数(到期前N天)',
  `trigger_time` TIME DEFAULT NULL COMMENT '触发时间',
  `remind_channels` VARCHAR(100) DEFAULT 'email,dingtalk' COMMENT '提醒渠道(逗号分隔): email邮件/dingtalk钉钉',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒规则表';

-- 8. 审核锁表 (audit_locks)
CREATE TABLE `audit_locks` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cert_id` BIGINT(20) NOT NULL COMMENT '健康证ID',
  `admin_id` BIGINT(20) NOT NULL COMMENT '锁定管理员ID',
  `admin_name` VARCHAR(100) NOT NULL COMMENT '锁定管理员姓名',
  `locked_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
  `expires_at` DATETIME NOT NULL COMMENT '锁定过期时间(5分钟后自动释放)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_id` (`cert_id`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核锁表(防止并发审核)';
