-- 团队奖资金记录菜单（已有库增量执行）
SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队奖资金记录', NULL, 'admin:system:team:level:brokerage:record', '/distribution/teamBrokerageRecord', 'C', 8, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamBrokerageRecord' LIMIT 1);
