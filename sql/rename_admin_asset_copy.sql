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

-- ============================================================
-- 收尾（2026-09-30）：数据库里剩下的「积分」一并改成「信用值」
-- 覆盖：权限按钮名、定时任务备注、附件名、动态表单、组合数据、装修链接、
--       小程序订阅消息模板、以及用户信用值明细的历史文案。
-- 幂等：以「含积分」为条件 + REPLACE，可重复执行。
-- ============================================================

-- 权限按钮名 / 分类名（如「修改积分余额」）
UPDATE `eb_system_menu` SET `name` = REPLACE(`name`, '积分', '信用值'), `update_time` = NOW()
WHERE `name` LIKE '%积分%';

UPDATE `eb_category` SET `name` = REPLACE(`name`, '积分', '信用值')
WHERE `name` LIKE '%积分%';

-- 定时任务备注（其余任务）
UPDATE `eb_schedule_job` SET `remark` = REPLACE(`remark`, '积分', '信用值')
WHERE `remark` LIKE '%积分%';

-- 附件名
UPDATE `eb_system_attachment` SET `name` = REPLACE(`name`, '积分', '信用值')
WHERE `name` LIKE '%积分%';

-- 动态表单（含非 109 的签到表单）
UPDATE `eb_system_form_temp`
SET `name` = REPLACE(`name`, '积分', '信用值'),
    `info` = REPLACE(`info`, '积分', '信用值'),
    `content` = REPLACE(`content`, '积分', '信用值'),
    `update_time` = NOW()
WHERE `content` LIKE '%积分%' OR `name` LIKE '%积分%' OR `info` LIKE '%积分%';

-- 组合数据（会员中心入口链接名：积分详情 → 信用值详情）
UPDATE `eb_system_group_data` SET `value` = REPLACE(`value`, '积分', '信用值'), `update_time` = NOW()
WHERE `value` LIKE '%积分%';

-- 装修页面（DIY 链接名：积分商城 → 信用值商城）
UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, '积分', '信用值'), `update_time` = NOW()
WHERE `value` LIKE '%积分%';

-- 小程序订阅消息模板标题
UPDATE `eb_wechat_program_public_temp` SET `title` = REPLACE(`title`, '积分', '信用值'), `update_time` = NOW()
WHERE `title` LIKE '%积分%';

-- 用户信用值明细的历史文案（老数据写「积分」，新数据代码已写「信用值」）
UPDATE `eb_user_integral_record` SET `title` = REPLACE(`title`, '积分', '信用值')
WHERE `title` LIKE '%积分%';

UPDATE `eb_user_integral_record` SET `mark` = REPLACE(`mark`, '积分', '信用值')
WHERE `mark` LIKE '%积分%';
