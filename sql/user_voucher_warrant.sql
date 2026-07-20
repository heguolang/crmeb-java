-- 消费券与权证体系
-- 执行前请备份数据库

-- 用户表增加字段（若已存在请手动跳过本段）
SET @exist_voucher := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'consume_voucher');
SET @sql_voucher := IF(@exist_voucher = 0, 'ALTER TABLE `eb_user` ADD COLUMN `consume_voucher` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT ''消费券'' AFTER `integral`', 'SELECT 1');
PREPARE stmt FROM @sql_voucher; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_warrant := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'warrant');
SET @sql_warrant := IF(@exist_warrant = 0, 'ALTER TABLE `eb_user` ADD COLUMN `warrant` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT ''权证（仅展示）'' AFTER `consume_voucher`', 'SELECT 1');
PREPARE stmt FROM @sql_warrant; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `eb_user_voucher_record` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录id',
    `uid` int(11) NOT NULL COMMENT '用户uid',
    `link_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '关联id',
    `link_type` varchar(32) NOT NULL DEFAULT '' COMMENT '关联类型：exchange/daily_release/to_balance/to_warrant/system',
    `type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '类型：1-增加，2-扣减',
    `title` varchar(64) NOT NULL DEFAULT '' COMMENT '标题',
    `voucher` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '变动消费券',
    `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '剩余消费券',
    `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `status` tinyint(1) NOT NULL DEFAULT 3 COMMENT '状态：3-完成',
    `create_time` datetime DEFAULT NULL COMMENT '添加时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_uid` (`uid`),
    KEY `idx_link_type` (`link_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消费券记录表';

CREATE TABLE IF NOT EXISTS `eb_user_warrant_record` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录id',
    `uid` int(11) NOT NULL COMMENT '用户uid',
    `link_id` varchar(32) NOT NULL DEFAULT '0' COMMENT '关联id',
    `link_type` varchar(32) NOT NULL DEFAULT '' COMMENT '关联类型：exchange/system',
    `type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '类型：1-增加，2-扣减',
    `title` varchar(64) NOT NULL DEFAULT '' COMMENT '标题',
    `warrant` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '变动权证',
    `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '剩余权证',
    `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
    `status` tinyint(1) NOT NULL DEFAULT 3 COMMENT '状态：3-完成',
    `create_time` datetime DEFAULT NULL COMMENT '添加时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_uid` (`uid`),
    KEY `idx_link_type` (`link_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权证记录表';

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'integral_to_voucher_ratio', '100', '多少积分=1消费券', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'integral_to_voucher_ratio');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'integral_daily_release_ratio', '10', '每日强制释放当前积分的百分比', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'integral_daily_release_ratio');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'voucher_to_balance_ratio', '10', '多少消费券=1元余额', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'voucher_to_balance_ratio');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'warrant_need_voucher', '5', '兑1权证所需消费券', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'warrant_need_voucher');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'warrant_need_integral', '100', '兑1权证所需积分', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'warrant_need_integral');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'voucher_warrant_switch', '1', '消费券权证功能开关：0=关闭，1=开启', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'voucher_warrant_switch');

INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'IntegralDailyReleaseTask', 'dailyRelease', '', '0 0 1 * * ?', 0, '每日积分强制释放到消费券', 0, NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'IntegralDailyReleaseTask' AND `method_name` = 'dailyRelease'
);

-- 后台菜单：挂到营销模块下（与积分菜单同级）
SET @marketingMenuId := (SELECT id FROM eb_system_menu WHERE component = '/marketing' AND menu_type = 'M' LIMIT 1);
SET @integralParentId := (SELECT pid FROM eb_system_menu WHERE component = '/marketing/integral' AND menu_type = 'M' LIMIT 1);
SET @parentMenuId := IFNULL(@marketingMenuId, @integralParentId);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @parentMenuId, '消费券权证', NULL, '', '/marketing/voucherWarrant', 'M', 90, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @parentMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/marketing/voucherWarrant' LIMIT 1);

SET @voucherParentId := (SELECT id FROM eb_system_menu WHERE component = '/marketing/voucherWarrant' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @voucherParentId, '消费券权证配置', NULL, 'admin:voucher:warrant:config', '/marketing/voucherWarrant/config', 'C', 1, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @voucherParentId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/marketing/voucherWarrant/config' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @voucherParentId, '消费券流水', NULL, 'admin:user:voucher:list', '/marketing/voucherWarrant/voucherLog', 'C', 2, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @voucherParentId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/marketing/voucherWarrant/voucherLog' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @voucherParentId, '权证流水', NULL, 'admin:user:warrant:list', '/marketing/voucherWarrant/warrantLog', 'C', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @voucherParentId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/marketing/voucherWarrant/warrantLog' LIMIT 1);
