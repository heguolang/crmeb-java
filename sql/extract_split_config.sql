-- 佣金/余额分拆提现设置
-- 执行前请备份数据库
-- 重要：必须用 UTF-8 连接执行，否则菜单中文会变成 ???
SET NAMES utf8mb4;

-- 提现来源字段
SET @exist_source := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_extract' AND COLUMN_NAME = 'extract_source');
SET @sql_source := IF(@exist_source = 0, 'ALTER TABLE `eb_user_extract` ADD COLUMN `extract_source` varchar(20) NOT NULL DEFAULT ''brokerage'' COMMENT ''提现来源：brokerage=佣金 balance=余额'' AFTER `uid`', 'SELECT 1');
PREPARE stmt FROM @sql_source; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 佣金提现配置
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'brokerage_extract_switch', '1', '佣金提现开关：0=关闭，1=开启', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'brokerage_extract_switch');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_fee_type', 'fixed', '佣金提现手续费类型：fixed=固定金额 percent=比例', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_fee_type');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_fee', '0', '佣金提现手续费值（固定元或百分比）', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_fee');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_multiple', '0', '佣金提现倍数：0=不限制，如10则只能提10/20/30…', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_multiple');

-- 余额提现配置（默认关闭）
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_switch', '0', '余额提现开关：0=关闭，1=开启', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_switch');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_min_price', '1', '余额提现最低金额', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_min_price');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_fee_type', 'fixed', '余额提现手续费类型：fixed=固定金额 percent=比例', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_fee_type');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_fee', '0', '余额提现手续费值（固定元或百分比）', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_fee');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_multiple', '0', '余额提现倍数：0=不限制，如100则只能提100/200/300…', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_multiple');

-- 后台菜单：财务操作下增加「提现设置」
SET @financeOpMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial/commission' AND menu_type = 'M' LIMIT 1);
SET @financeMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial' AND menu_type = 'M' LIMIT 1);
SET @parentMenuId := IFNULL(@financeOpMenuId, @financeMenuId);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @parentMenuId, '提现设置', NULL, 'admin:finance:extract:config', '/financial/commission/extractConfig', 'C', 2, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @parentMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/commission/extractConfig' LIMIT 1);
