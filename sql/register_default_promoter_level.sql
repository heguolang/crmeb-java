-- 用户首次注册/登录：是否默认推广员、默认会员等级
-- 执行前请备份；建议：mysql --default-character-set=utf8mb4 ...

SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'register_default_is_promoter', '0', '注册默认推广员', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'register_default_is_promoter');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'register_default_user_level', '0', '注册默认会员等级', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'register_default_user_level');
