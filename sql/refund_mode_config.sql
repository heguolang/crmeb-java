-- 退款方式配置：1=原路退回 2=退款到余额
-- 背景：微信商户号受限/换号期间原路退回不可用，管理员可把退款直接退入用户余额。
-- 幂等：已存在则跳过；发布后需清 Redis 配置缓存 config_list。
-- ⚠ 本表 status 语义反转：SystemConfigServiceImpl.getByName 固定查 status=0，故 status 必须 0 才会被读到。
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'refund_mode', '默认退款方式：1=原路退回 2=退款到余额', 0, '2', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'refund_mode');

UPDATE `eb_system_config` SET `status` = 0 WHERE `name` = 'refund_mode' AND `status` <> 0;

