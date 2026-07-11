-- 团队等级 + 团队等级配置（独立于会员等级体系）
-- 执行前请备份数据库

CREATE TABLE IF NOT EXISTS `eb_system_team_level` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(50) NOT NULL DEFAULT '' COMMENT '团队等级名称',
    `grade` int(11) NOT NULL DEFAULT 1 COMMENT '团队等级序号，数值越大等级越高',
    `self_order_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '自购订单金额门槛(元)',
    `team_order_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '团队订单金额门槛(元)',
    `self_order_trigger_type` tinyint(1) NOT NULL DEFAULT 2 COMMENT '自购订单统计时机：1=支付成功，2=订单完成',
    `team_order_trigger_type` tinyint(1) NOT NULL DEFAULT 2 COMMENT '团队订单统计时机：1=支付成功，2=订单完成',
    `description` varchar(500) DEFAULT NULL COMMENT '等级权益描述',
    `icon` varchar(255) NOT NULL DEFAULT '' COMMENT '等级图标',
    `is_show` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否显示：1=显示，0=隐藏',
    `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_grade` (`grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队等级表';

CREATE TABLE IF NOT EXISTS `eb_system_team_level_config` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `team_level_id` int(11) NOT NULL COMMENT '团队等级ID，关联 eb_system_team_level.id',
    `team_brokerage_rate` int(11) NOT NULL DEFAULT 0 COMMENT '团队极差比例(%)',
    `peer_award_rate` int(11) NOT NULL DEFAULT 0 COMMENT '平级奖比例(%)',
    `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_level_id` (`team_level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队等级配置表';

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'team_brokerage_status', '0', '团队极差奖开关：0=关闭，1=开启', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'team_brokerage_status');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'team_brokerage_max_depth', '0', '团队奖向上追溯层数：0=不限', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'team_brokerage_max_depth');

-- 后台菜单/权限（不写死 id/pid，避免不同库数据不一致导致挂错）
-- 说明：按钮权限建议挂在“团队等级”菜单下，便于权限树展示与勾选
SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队等级', NULL, 'admin:system:team:level:list', '/distribution/teamGrade', 'C', 4, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamGrade' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队等级配置', NULL, 'admin:system:team:level:list', '/distribution/teamLevelConfig', 'C', 5, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamLevelConfig' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队关联用户', NULL, 'admin:system:team:level:user:list', '/distribution/teamUser', 'C', 6, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamUser' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队变更记录', NULL, 'admin:system:team:level:record:list', '/distribution/teamRecord', 'C', 7, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamRecord' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队奖资金记录', NULL, 'admin:system:team:level:brokerage:record', '/distribution/teamBrokerageRecord', 'C', 8, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/teamBrokerageRecord' LIMIT 1);

SET @teamGradeMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution/teamGrade' AND menu_type = 'C' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @teamGradeMenuId, '团队等级添加', NULL, 'admin:system:team:level:save', '', 'A', 1, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @teamGradeMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid` = @teamGradeMenuId AND `perms` = 'admin:system:team:level:save' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @teamGradeMenuId, '团队等级修改', NULL, 'admin:system:team:level:update', '', 'A', 2, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @teamGradeMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid` = @teamGradeMenuId AND `perms` = 'admin:system:team:level:update' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @teamGradeMenuId, '团队等级删除', NULL, 'admin:system:team:level:delete', '', 'A', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @teamGradeMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid` = @teamGradeMenuId AND `perms` = 'admin:system:team:level:delete' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @teamGradeMenuId, '团队等级启用/禁用', NULL, 'admin:system:team:level:use', '', 'A', 4, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @teamGradeMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid` = @teamGradeMenuId AND `perms` = 'admin:system:team:level:use' LIMIT 1);
