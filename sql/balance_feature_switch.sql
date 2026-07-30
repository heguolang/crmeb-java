-- 余额功能开关：余额互转 / 佣金转余额 / 余额充值
-- 执行前请备份数据库
-- 重要：必须用 UTF-8 连接执行，否则菜单中文会变成 ???
SET NAMES utf8mb4;

-- 余额互转开关（默认开启）
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_transfer_switch', '1', '余额互转开关：0=关闭，1=开启', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_transfer_switch');

-- 佣金转余额开关（默认开启）
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'brokerage_to_yue_switch', '1', '佣金转余额开关：0=关闭，1=开启', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'brokerage_to_yue_switch');

-- 余额充值开关：与既有 recharge_switch 兼容，若已存在则不覆盖
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `form_id`, `status`)
SELECT 'recharge_switch', 'true', '余额充值开关', 78, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'recharge_switch');

-- 后台菜单：财务操作下增加「余额功能设置」
SET @financeOpMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial/commission' AND menu_type = 'M' LIMIT 1);
SET @financeMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial' AND menu_type = 'M' LIMIT 1);
SET @parentMenuId := IFNULL(@financeOpMenuId, @financeMenuId);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @parentMenuId, '余额功能设置', NULL, 'admin:finance:balance:feature:config', '/financial/commission/balanceFeature', 'C', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @parentMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/commission/balanceFeature' LIMIT 1);
