-- ============================================
-- 迁移脚本：移除健康证编号唯一索引
-- 允许健康证编号重复，但通过应用层保证is_current=1时编号唯一
-- ============================================

-- 1. 移除原有的唯一索引
ALTER TABLE `health_certificates` DROP INDEX `uk_cert_number`;

-- 2. 添加普通索引（用于查询性能）
ALTER TABLE `health_certificates` ADD INDEX `idx_cert_number` (`cert_number`);

-- 3. 添加复合唯一索引：保证is_current=1时，cert_number唯一
-- 注意：MySQL不支持部分唯一索引，所以我们需要在应用层保证这个约束
-- 这里只添加普通索引用于查询优化
ALTER TABLE `health_certificates` ADD INDEX `idx_cert_number_is_current` (`cert_number`, `is_current`);

-- 说明：
-- 由于MySQL不支持部分唯一索引（Partial Unique Index），
-- 我们无法在数据库层面直接保证"is_current=1时cert_number唯一"。
-- 因此，这个约束需要在应用层（Java代码）中保证。
-- 
-- 应用层约束逻辑：
-- 1. 提交新健康证时，如果编号已存在且is_current=1，先将旧的设为is_current=0
-- 2. 审核通过时，将同一员工的其他健康证设为is_current=0
-- 3. 查询当前有效健康证时，使用is_current=1条件

