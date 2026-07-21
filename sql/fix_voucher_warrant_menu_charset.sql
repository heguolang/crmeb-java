-- 修复「消费券权证」菜单中文因执行 SQL 时客户端编码错误变成问号的问题
-- 执行前请先：SET NAMES utf8mb4;

SET NAMES utf8mb4;

UPDATE `eb_system_menu` SET `name` = '消费券权证', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant' AND `menu_type` = 'M';

UPDATE `eb_system_menu` SET `name` = '消费券权证配置', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/config' AND `menu_type` = 'C';

UPDATE `eb_system_menu` SET `name` = '消费券流水', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/voucherLog' AND `menu_type` = 'C';

UPDATE `eb_system_menu` SET `name` = '权证流水', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/warrantLog' AND `menu_type` = 'C';

-- 可选：修复配置项标题（若配置分类里也是问号）
UPDATE `eb_system_config` SET `title` = '多少积分=1消费券' WHERE `name` = 'integral_to_voucher_ratio';
UPDATE `eb_system_config` SET `title` = '每日强制释放当前积分的百分比' WHERE `name` = 'integral_daily_release_ratio';
UPDATE `eb_system_config` SET `title` = '多少消费券=1元余额' WHERE `name` = 'voucher_to_balance_ratio';
UPDATE `eb_system_config` SET `title` = '多少消费券=1权证（单独兑换）' WHERE `name` = 'warrant_need_voucher';
UPDATE `eb_system_config` SET `title` = '多少积分=1权证（单独兑换）' WHERE `name` = 'warrant_need_integral';
UPDATE `eb_system_config` SET `title` = '消费券权证功能开关：0=关闭，1=开启' WHERE `name` = 'voucher_warrant_switch';

UPDATE `eb_schedule_job` SET `remark` = '每日积分强制释放到消费券'
WHERE `bean_name` = 'IntegralDailyReleaseTask' AND `method_name` = 'dailyRelease';
