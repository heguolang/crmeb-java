-- 消费券权证：兑换开关 / 释放开关拆分
-- 执行前请备份数据库
SET NAMES utf8mb4;

-- 释放开关：默认继承原「功能开关」取值，避免行为突变
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'integral_daily_release_switch',
       IFNULL((SELECT `value` FROM `eb_system_config` WHERE `name` = 'voucher_warrant_switch' LIMIT 1), '1'),
       '积分每日释放开关：0=关闭，1=开启',
       0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'integral_daily_release_switch');

UPDATE `eb_system_config`
SET `title` = '消费券权证兑换开关：0=关闭，1=开启'
WHERE `name` = 'voucher_warrant_switch';
