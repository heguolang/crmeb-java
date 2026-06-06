-- 会员等级配置扩展：升级条件 + 固定赠送积分权益
-- 执行前请备份数据库

ALTER TABLE `eb_system_user_level`
    ADD COLUMN `upgrade_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '升级条件类型：1=累计消费金额，2=累计订单数，3=两者同时满足' AFTER `experience`,
    ADD COLUMN `consumption_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '消费金额统计时机：1=已付款，2=交易完成' AFTER `upgrade_type`,
    ADD COLUMN `order_count_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '订单数统计时机：1=已付款，2=交易完成' AFTER `consumption_trigger_type`,
    ADD COLUMN `upgrade_value` int(11) NOT NULL DEFAULT 0 COMMENT '累计订单数升级门槛' AFTER `order_count_trigger_type`,
    ADD COLUMN `give_integral` int(11) NOT NULL DEFAULT 0 COMMENT '等级赠送积分（每单固定赠送，手输多少送多少）' AFTER `upgrade_value`,
    ADD COLUMN `description` varchar(500) DEFAULT NULL COMMENT '等级权益描述' AFTER `give_integral`;

ALTER TABLE `eb_user_level`
    ADD COLUMN `give_integral` int(11) NOT NULL DEFAULT 0 COMMENT '等级赠送积分' AFTER `discount`;

-- 若已执行过 integral_multiplier 版本，可改用以下语句迁移：
-- ALTER TABLE `eb_system_user_level` CHANGE COLUMN `integral_multiplier` `give_integral` int(11) NOT NULL DEFAULT 0 COMMENT '等级赠送积分（每单固定赠送，手输多少送多少）';
-- ALTER TABLE `eb_user_level` CHANGE COLUMN `integral_multiplier` `give_integral` int(11) NOT NULL DEFAULT 0 COMMENT '等级赠送积分';

-- 示例：创客等级（消费满126元，每单赠送200积分）
-- INSERT INTO `eb_system_user_level`
-- (`name`, `experience`, `upgrade_type`, `upgrade_value`, `give_integral`, `description`, `is_show`, `grade`, `discount`, `icon`, `is_del`, `create_time`, `update_time`)
-- VALUES
-- ('创客', 126, 3, 1, 200, '累计消费满126元且下单1次升级，每单赠送200积分', 1, 1, 100, '', 0, NOW(), NOW());
