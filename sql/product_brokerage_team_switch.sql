-- 商品是否参与分销、是否参与团队奖（默认参与）
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

SET @exist_brokerage := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eb_store_product'
      AND COLUMN_NAME = 'is_brokerage'
);
SET @sql_brokerage := IF(
    @exist_brokerage = 0,
    'ALTER TABLE `eb_store_product` ADD COLUMN `is_brokerage` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否参与分销：0=否 1=是'' AFTER `is_sub`',
    'SELECT 1'
);
PREPARE stmt FROM @sql_brokerage;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_team := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eb_store_product'
      AND COLUMN_NAME = 'is_team_brokerage'
);
SET @sql_team := IF(
    @exist_team = 0,
    'ALTER TABLE `eb_store_product` ADD COLUMN `is_team_brokerage` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否参与团队奖：0=否 1=是'' AFTER `is_brokerage`',
    'SELECT 1'
);
PREPARE stmt FROM @sql_team;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
