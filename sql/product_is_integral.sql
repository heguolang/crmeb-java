-- 商品是否支持积分抵扣（默认不支持）
-- 重要：必须用 UTF-8 连接执行
SET NAMES utf8mb4;

SET @exist_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eb_store_product'
      AND COLUMN_NAME = 'is_integral'
);
SET @sql_col := IF(
    @exist_col = 0,
    'ALTER TABLE `eb_store_product` ADD COLUMN `is_integral` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持积分抵扣：0=否 1=是'' AFTER `give_integral`',
    'SELECT 1'
);
PREPARE stmt FROM @sql_col;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
