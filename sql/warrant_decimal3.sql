-- 权证精度扩展到 3 位小数（支持 0.001）
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

-- 用户权证余额
ALTER TABLE `eb_user`
  MODIFY COLUMN `warrant` decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '权证（支持到0.001）';

-- 权证流水
ALTER TABLE `eb_user_warrant_record`
  MODIFY COLUMN `warrant` decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '变动权证',
  MODIFY COLUMN `balance` decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '剩余权证';

-- 权证兑换申请
ALTER TABLE `eb_user_warrant_exchange`
  MODIFY COLUMN `warrant_amount` decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '兑换权证数量';
