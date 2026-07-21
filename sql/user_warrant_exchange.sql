-- 权证兑换申请（类似提现，后台人工处理）
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_user_warrant_exchange` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT '用户UID',
  `pay_type` varchar(20) NOT NULL DEFAULT '' COMMENT '支付方式：integral=积分 voucher=消费券',
  `pay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际消耗积分或消费券',
  `warrant_amount` decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '兑换权证数量',
  `address` varchar(255) NOT NULL DEFAULT '' COMMENT '权证第三方地址',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0=待处理 1=已处理',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '申请时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权证兑换申请';

-- 后台菜单：财务操作下增加「权证兑换」
SET @financeOpMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial/commission' AND menu_type = 'M' LIMIT 1);
SET @financeMenuId := (SELECT id FROM eb_system_menu WHERE component = '/financial' AND menu_type = 'M' LIMIT 1);
SET @parentMenuId := IFNULL(@financeOpMenuId, @financeMenuId);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @parentMenuId, '权证兑换', NULL, 'admin:finance:warrant:exchange:list', '/financial/commission/warrantExchange', 'C', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @parentMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/commission/warrantExchange' LIMIT 1);
