-- 提现倍数配置（可单独执行；utf8mb4）
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_multiple', '0', '佣金提现倍数：0=不限制，如10则只能提10/20/30…', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_multiple');

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'balance_extract_multiple', '0', '余额提现倍数：0=不限制，如100则只能提100/200/300…', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'balance_extract_multiple');
