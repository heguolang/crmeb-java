-- 提现支持账户配置（银行卡/微信/支付宝）
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'user_extract_type', 'bank,weixin,alipay', '提现支持方式：bank=银行卡 weixin=微信 alipay=支付宝，逗号分隔', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'user_extract_type');
