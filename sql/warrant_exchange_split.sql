-- 权证兑换改为积分/消费券各自独立兑换（更新配置说明）
SET NAMES utf8mb4;

UPDATE `eb_system_config` SET `title` = '多少消费券=1权证（单独兑换）' WHERE `name` = 'warrant_need_voucher';
UPDATE `eb_system_config` SET `title` = '多少积分=1权证（单独兑换）' WHERE `name` = 'warrant_need_integral';
