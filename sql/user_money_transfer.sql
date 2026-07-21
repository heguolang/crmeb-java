-- 用户余额互转
-- 重要：必须用 UTF-8 连接执行，否则菜单中文会变成 ???
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_user_money_transfer` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transfer_no` varchar(32) NOT NULL DEFAULT '' COMMENT '转账单号',
  `from_uid` int(11) NOT NULL DEFAULT 0 COMMENT '转出用户UID',
  `to_uid` int(11) NOT NULL DEFAULT 0 COMMENT '转入用户UID',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '转账金额',
  `from_balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '转出后余额',
  `to_balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '转入后余额',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1=成功',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_from_uid` (`from_uid`),
  KEY `idx_to_uid` (`to_uid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户余额互转记录';

-- 后台菜单：财务记录下增加「余额转账」
SET @financeRecordId := (SELECT id FROM eb_system_menu WHERE component = '/financial/record' AND menu_type = 'M' LIMIT 1);
SET @financeMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial' AND menu_type = 'M' LIMIT 1);
SET @parentMenuId := IFNULL(@financeRecordId, @financeMenuId);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @parentMenuId, '余额转账', NULL, 'admin:finance:transfer:list', '/financial/record/transfer', 'C', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @parentMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/record/transfer' LIMIT 1);
