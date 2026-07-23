-- 积分 / 分销佣金 / 团队奖 到账方式配置
-- 1=支付订单到账（默认，兼容现有行为） 2=订单完成（确认收货）后到账
-- 执行前请备份；建议：mysql --default-character-set=utf8mb4 ...

SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'integral_credit_timing', '1', '积分到账方式', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'integral_credit_timing');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'brokerage_credit_timing', '1', '分销佣金到账方式', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'brokerage_credit_timing');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'team_brokerage_credit_timing', '1', '团队奖到账方式', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'team_brokerage_credit_timing');
