-- 用户团队等级：统计 + 当前等级记录
-- 执行前请备份数据库

-- 1) 用户表增加当前团队等级字段（独立于会员等级 level）
ALTER TABLE `eb_user`
    ADD COLUMN `team_level` int(11) NOT NULL DEFAULT 0 COMMENT '团队等级ID（eb_system_team_level.id），0=无' AFTER `level`;

-- 2) 团队等级统计（累计金额，分别按支付/完成统计）
CREATE TABLE IF NOT EXISTS `eb_user_team_level_stat` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid` int(11) NOT NULL COMMENT '用户uid',
    `self_paid_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '自购已支付累计金额(元)',
    `self_complete_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '自购已完成累计金额(元)',
    `team_paid_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '团队已支付累计金额(元)',
    `team_complete_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '团队已完成累计金额(元)',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户团队等级统计表';

-- 3) 团队等级变更记录（类似 eb_user_level）
CREATE TABLE IF NOT EXISTS `eb_user_team_level` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `uid` int(11) NOT NULL COMMENT '用户uid',
    `team_level_id` int(11) NOT NULL DEFAULT 0 COMMENT '团队等级ID',
    `grade` int(11) NOT NULL DEFAULT 0 COMMENT '团队等级序号',
    `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0:禁止,1:正常',
    `mark` varchar(255) DEFAULT NULL COMMENT '备注',
    `remind` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已通知',
    `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除,0=未删除,1=删除',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户团队等级记录表';

