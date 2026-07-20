-- 权证第三方地址绑定
-- 执行前请备份数据库

SET @exist_addr := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'warrant_address');
SET @sql_addr := IF(@exist_addr = 0, 'ALTER TABLE `eb_user` ADD COLUMN `warrant_address` varchar(255) NOT NULL DEFAULT '''' COMMENT ''权证第三方地址'' AFTER `warrant`', 'SELECT 1');
PREPARE stmt FROM @sql_addr; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_addr_time := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'warrant_address_time');
SET @sql_addr_time := IF(@exist_addr_time = 0, 'ALTER TABLE `eb_user` ADD COLUMN `warrant_address_time` datetime DEFAULT NULL COMMENT ''权证地址最近绑定时间'' AFTER `warrant_address`', 'SELECT 1');
PREPARE stmt FROM @sql_addr_time; EXECUTE stmt; DEALLOCATE PREPARE stmt;
