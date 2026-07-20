-- 提现手续费配置
-- 执行前请备份数据库
-- 若字段已存在会报错，可忽略对应语句

ALTER TABLE `eb_user_extract`
    ADD COLUMN `extract_fee` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '提现手续费' AFTER `extract_price`;

ALTER TABLE `eb_user_extract`
    ADD COLUMN `arrive_price` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '实际到账金额' AFTER `extract_fee`;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_fee', '0', '提现手续费（元），固定收取', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_fee');
