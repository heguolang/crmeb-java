-- 会员等级返佣配置关联表（与 eb_system_user_level 一对一）
-- 执行前请备份数据库

CREATE TABLE IF NOT EXISTS `eb_system_user_level_brokerage` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `level_id` int(11) NOT NULL COMMENT '会员等级ID，关联 eb_system_user_level.id',
    `self_brokerage_rate` int(11) NOT NULL DEFAULT 0 COMMENT '自购返佣比例(%)',
    `brokerage_rate_one` int(11) NOT NULL DEFAULT 0 COMMENT '一级返佣比例(%)',
    `brokerage_rate_two` int(11) NOT NULL DEFAULT 0 COMMENT '二级返佣比例(%)',
    `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_id` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级返佣配置表';
