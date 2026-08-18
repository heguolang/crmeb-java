-- 管理后台文案：积分→信用值，消费券→CCEA，权证/MLSS→CEA
-- 执行前请备份；必须用 UTF-8 连接：
-- mysql --default-character-set=utf8mb4 ...
SET NAMES utf8mb4;

-- 侧边栏 / 菜单
UPDATE `eb_system_menu` SET `name` = '信用值', `update_time` = NOW()
WHERE `component` = '/marketing/integral' AND `menu_type` = 'M';

UPDATE `eb_system_menu` SET `name` = '信用值配置', `update_time` = NOW()
WHERE `component` = '/marketing/integral/integralconfig';

UPDATE `eb_system_menu` SET `name` = '信用值日志', `update_time` = NOW()
WHERE `component` = '/marketing/integral/integrallog';

UPDATE `eb_system_menu` SET `name` = 'CCEA CEA', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant' AND `menu_type` = 'M';

UPDATE `eb_system_menu` SET `name` = 'CCEA CEA配置', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/config';

UPDATE `eb_system_menu` SET `name` = 'CCEA流水', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/voucherLog';

UPDATE `eb_system_menu` SET `name` = 'CEA流水', `update_time` = NOW()
WHERE `component` = '/marketing/voucherWarrant/warrantLog';

UPDATE `eb_system_menu` SET `name` = 'CEA兑换', `update_time` = NOW()
WHERE `component` = '/financial/commission/warrantExchange';

-- 兼容旧分类菜单（若仍在使用）
UPDATE `eb_category` SET `name` = '信用值' WHERE `name` = '积分' AND `url` LIKE '%/marketing/integral';
UPDATE `eb_category` SET `name` = '信用值配置' WHERE `name` = '积分配置';
UPDATE `eb_category` SET `name` = '信用值日志' WHERE `name` IN ('积分日志', '积分流水');

-- 系统配置项标题
UPDATE `eb_system_config` SET `title` = '多少信用值=1CCEA（主动兑换）' WHERE `name` = 'integral_to_voucher_ratio';
UPDATE `eb_system_config` SET `title` = '每日强制释放当前信用值的百分比' WHERE `name` = 'integral_daily_release_ratio';
UPDATE `eb_system_config` SET `title` = '每日释放：多少信用值=1CCEA' WHERE `name` = 'integral_daily_release_exchange_ratio';
UPDATE `eb_system_config` SET `title` = '多少CCEA=1元余额' WHERE `name` = 'voucher_to_balance_ratio';
UPDATE `eb_system_config` SET `title` = '多少CCEA=1CEA（单独兑换）' WHERE `name` = 'warrant_need_voucher';
UPDATE `eb_system_config` SET `title` = '多少信用值=1CEA（单独兑换）' WHERE `name` = 'warrant_need_integral';
UPDATE `eb_system_config` SET `title` = 'CCEA CEA兑换开关：0=关闭，1=开启' WHERE `name` = 'voucher_warrant_switch';
UPDATE `eb_system_config` SET `title` = '信用值每日释放开关：0=关闭，1=开启' WHERE `name` = 'integral_daily_release_switch';
UPDATE `eb_system_config` SET `title` = '信用值到账方式' WHERE `name` = 'integral_credit_timing';

-- 积分配置动态表单（信用值抵用比例等）
UPDATE `eb_system_form_temp`
SET `name` = REPLACE(`name`, '积分', '信用值'),
    `info` = REPLACE(`info`, '积分', '信用值'),
    `content` = REPLACE(`content`, '积分', '信用值'),
    `update_time` = NOW()
WHERE `id` = 109 OR `name` = '积分设置';

-- 定时任务备注
UPDATE `eb_schedule_job` SET `remark` = '每日信用值强制释放到CCEA'
WHERE `bean_name` = 'IntegralDailyReleaseTask' AND `method_name` = 'dailyRelease';
