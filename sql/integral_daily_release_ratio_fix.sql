-- 积分每日释放：独立兑换比例 + 积分支持小数
-- 执行前请备份数据库
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

-- 用户积分改为小数（支持每日释放扣减 0.99 等）
ALTER TABLE `eb_user`
    MODIFY COLUMN `integral` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '用户剩余积分';

-- 积分流水同步支持小数
ALTER TABLE `eb_user_integral_record`
    MODIFY COLUMN `integral` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '积分',
    MODIFY COLUMN `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '剩余';

-- 每日释放独立兑换比例：多少积分=1消费券（与主动兑换 integral_to_voucher_ratio 分离）
INSERT INTO `eb_system_config` (`name`, `value`, `title`, `status`)
SELECT 'integral_daily_release_exchange_ratio', '1', '每日释放：多少积分=1消费券', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'integral_daily_release_exchange_ratio');

-- 每日释放百分比默认改为 1%，并更新说明
UPDATE `eb_system_config`
SET `value` = '1',
    `title` = '每日强制释放当前积分的百分比'
WHERE `name` = 'integral_daily_release_ratio';
