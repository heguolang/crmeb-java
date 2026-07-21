package com.zbkj.common.constants;

/**
 *  系统设置常量类
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
public class SysConfigConstants {

    //后台首页登录图片
    /** 登录页LOGO */
    public static final String CONFIG_KEY_ADMIN_LOGIN_LOGO_LEFT_TOP = "site_logo_lefttop";
    public static final String CONFIG_KEY_ADMIN_LOGIN_LOGO_LOGIN = "site_logo_login";
    public static final String CONFIG_KEY_ADMIN_LOGO_SQUARE = "site_logo_square";
    public static final String CONFIG_KEY_SITE_NAME = "site_name";
    /** 登录页背景图 */
    public static final String CONFIG_KEY_ADMIN_LOGIN_BACKGROUND_IMAGE = "admin_login_bg_pic";

    /** 微信分享图片（公众号） */
    public static final String CONFIG_KEY_ADMIN_WECHAT_SHARE_IMAGE = "wechat_share_img";
    /** 微信分享标题（公众号） */
    public static final String CONFIG_KEY_ADMIN_WECHAT_SHARE_TITLE = "wechat_share_title";
    /** 微信分享简介（公众号） */
    public static final String CONFIG_KEY_ADMIN_WECHAT_SHARE_SYNOSIS = "wechat_share_synopsis";


    /** 是否启用分销 */
    public static final String CONFIG_KEY_BROKERAGE_FUNC_STATUS = "brokerage_func_status";
    /** 分销模式 :1-指定分销，2-人人分销 */
    public static final String CONFIG_KEY_STORE_BROKERAGE_STATUS = "store_brokerage_status";
    /** 分销模式 :1-指定分销 */
    public static final String STORE_BROKERAGE_STATUS_APPOINT = "1";
    /** 分销模式 :2-人人分销 */
    public static final String STORE_BROKERAGE_STATUS_PEOPLE = "2";
    /** 一级返佣比例 */
    public static final String CONFIG_KEY_STORE_BROKERAGE_RATIO = "store_brokerage_ratio";
    /** 二级返佣比例 */
    public static final String CONFIG_KEY_STORE_BROKERAGE_TWO = "store_brokerage_two";
    /** 判断是否开启气泡 */
    public static final String CONFIG_KEY_STORE_BROKERAGE_IS_BUBBLE = "store_brokerage_is_bubble";
    /** 判断是否分销消费门槛 */
    public static final String CONFIG_KEY_STORE_BROKERAGE_QUOTA = "store_brokerage_quota";
    /** 是否启用团队极差奖 */
    public static final String CONFIG_KEY_TEAM_BROKERAGE_STATUS = "team_brokerage_status";
    /** 团队奖向上追溯层数，0=不限 */
    public static final String CONFIG_KEY_TEAM_BROKERAGE_MAX_DEPTH = "team_brokerage_max_depth";

    /** 是否开启会员功能 */
    public static final String CONFIG_KEY_VIP_OPEN = "vip_open";
    /** 是否开启充值功能 */
    public static final String CONFIG_KEY_RECHARGE_SWITCH = "recharge_switch";
    /** 是否开启门店自提 */
    public static final String CONFIG_KEY_STORE_SELF_MENTION = "store_self_mention";
//    /** 腾讯地图key */
    public static final String CONFIG_SITE_TENG_XUN_MAP_KEY = "tengxun_map_key";
    /** 退款理由 */
    public static final String CONFIG_KEY_STOR_REASON = "stor_reason";
    /** 提现最低金额（佣金） */
    public static final String CONFIG_EXTRACT_MIN_PRICE = "user_extract_min_price";
    /** 提现冻结时间 */
    public static final String CONFIG_EXTRACT_FREEZING_TIME = "extract_time";
    /** 佣金提现手续费值（固定元或百分比，随 fee_type） */
    public static final String CONFIG_EXTRACT_FEE = "user_extract_fee";
    /** 佣金提现手续费类型：fixed / percent */
    public static final String CONFIG_EXTRACT_FEE_TYPE = "user_extract_fee_type";
    /** 佣金提现开关：0关闭 1开启 */
    public static final String CONFIG_BROKERAGE_EXTRACT_SWITCH = "brokerage_extract_switch";
    /** 余额提现开关 */
    public static final String CONFIG_BALANCE_EXTRACT_SWITCH = "balance_extract_switch";
    /** 余额提现最低金额 */
    public static final String CONFIG_BALANCE_EXTRACT_MIN_PRICE = "balance_extract_min_price";
    /** 余额提现手续费类型 */
    public static final String CONFIG_BALANCE_EXTRACT_FEE_TYPE = "balance_extract_fee_type";
    /** 余额提现手续费值 */
    public static final String CONFIG_BALANCE_EXTRACT_FEE = "balance_extract_fee";
    /** 佣金提现倍数：0不限制 */
    public static final String CONFIG_EXTRACT_MULTIPLE = "user_extract_multiple";
    /** 余额提现倍数：0不限制 */
    public static final String CONFIG_BALANCE_EXTRACT_MULTIPLE = "balance_extract_multiple";
    /** 提现支持方式，逗号分隔：bank,weixin,alipay */
    public static final String CONFIG_USER_EXTRACT_TYPE = "user_extract_type";

    /** 提现方式：银行卡 */
    public static final String EXTRACT_TYPE_BANK = "bank";
    /** 提现方式：微信 */
    public static final String EXTRACT_TYPE_WEIXIN = "weixin";
    /** 提现方式：支付宝 */
    public static final String EXTRACT_TYPE_ALIPAY = "alipay";

    /** 提现来源：佣金 */
    public static final String EXTRACT_SOURCE_BROKERAGE = "brokerage";
    /** 提现来源：余额 */
    public static final String EXTRACT_SOURCE_BALANCE = "balance";
    /** 手续费类型：固定金额 */
    public static final String EXTRACT_FEE_TYPE_FIXED = "fixed";
    /** 手续费类型：比例 */
    public static final String EXTRACT_FEE_TYPE_PERCENT = "percent";

    /** 全场满额包邮开关 */
    public static final String STORE_FEE_POSTAGE_SWITCH = "store_free_postage_switch";
    /** 全场满额包邮金额 */
    public static final String STORE_FEE_POSTAGE = "store_free_postage";
    /** 积分抵用比例(1积分抵多少金额) */
    public static final String CONFIG_KEY_INTEGRAL_RATE = "integral_ratio";
    /** 下单支付金额按比例赠送积分（实际支付1元赠送多少积分) */
    public static final String CONFIG_KEY_INTEGRAL_RATE_ORDER_GIVE = "order_give_integral";

    /** 多少积分 = 1 消费券 */
    public static final String CONFIG_KEY_INTEGRAL_TO_VOUCHER_RATIO = "integral_to_voucher_ratio";
    /** 每日强制释放当前积分的百分比 */
    public static final String CONFIG_KEY_INTEGRAL_DAILY_RELEASE_RATIO = "integral_daily_release_ratio";
    /** 多少消费券 = 1 元余额 */
    public static final String CONFIG_KEY_VOUCHER_TO_BALANCE_RATIO = "voucher_to_balance_ratio";
    /** 兑 1 权证所需消费券 */
    public static final String CONFIG_KEY_WARRANT_NEED_VOUCHER = "warrant_need_voucher";
    /** 兑 1 权证所需积分 */
    public static final String CONFIG_KEY_WARRANT_NEED_INTEGRAL = "warrant_need_integral";
    /** 消费券权证功能开关：0关闭 1开启 */
    public static final String CONFIG_KEY_VOUCHER_WARRANT_SWITCH = "voucher_warrant_switch";

    /** 微信支付开关 */
    public static final String CONFIG_PAY_WEIXIN_OPEN  = "pay_weixin_open";
    /** 余额支付状态 */
    public static final String CONFIG_YUE_PAY_STATUS  = "yue_pay_status";
    /** 支付宝支付状态 */
    public static final String CONFIG_ALI_PAY_STATUS = "ali_pay_status";

    /** 版权-授权标签 */
    public static final String CONFIG_COPYRIGHT_LABEL = "copyright_label";
    /** 版权-公司信息 */
    public static final String CONFIG_COPYRIGHT_COMPANY_INFO = "copyright_company_name";
    /** 版权-公司图片 */
    public static final String CONFIG_COPYRIGHT_COMPANY_IMAGE = "copyright_company_image";
    /** 版权-授权地址 */
    public static final String CONFIG_COPYRIGHT_AUTH_HOST = "authHost";

    /** 主题测配置 */
    public static final String CONFIG_CHANGE_COLOR = "change_color_config";

    /** 电子面单-快递公司ID */
    public static final String ELECTRONIC_FACE_SHEET_EXPORT_ID = "config_export_id";
    /** 电子面单-快递公司模板id */
    public static final String ELECTRONIC_FACE_SHEET_EXPORT_TEMP_ID = "config_export_temp_id";
    /** 电子面单-快递公司编码 */
    public static final String ELECTRONIC_FACE_SHEET_EXPORT_COM = "config_export_com";
    /** 电子面单-发货人姓名 */
    public static final String ELECTRONIC_FACE_SHEET_TO_NAME = "config_export_to_name";
    /** 电子面单-发货人电话 */
    public static final String ELECTRONIC_FACE_SHEET_TO_TEL = "config_export_to_tel";
    /** 电子面单-发货人详细地址 */
    public static final String ELECTRONIC_FACE_SHEET_ADDRESS = "config_export_to_address";
    /** 电子面单-电子面单打印机编号 */
    public static final String ELECTRONIC_FACE_SHEET_PRINTER_NUM = "config_export_siid";
    /** 电子面单-开关 */
    public static final String ELECTRONIC_FACE_SHEET_OPEN = "config_export_open";


    /** 系统配置列表 */
    public static final String CONFIG_LIST = "config_list";
    /** 移动端域名 */
    public static final String CONFIG_KEY_SITE_URL = "site_url";
    /** 后台api地址(回调地址) */
    public static final String CONFIG_KEY_API_URL = "api_url";
    /** 移动商城api接口地址 */
    public static final String CONFIG_KEY_FRONT_API_URL = "front_api_url";

    /** 充值注意事项 */
    public static final String CONFIG_RECHARGE_ATTENTION = "recharge_attention";


    /** 图片上传类型 1本地 2七牛云 3OSS 4COS 5京东, 默认本地 */
    public static final String CONFIG_UPLOAD_TYPE = "uploadType";
    /** 文件上传是否保存本地 */
    public static final String CONFIG_FILE_IS_SAVE = "file_is_save";
    /** 全局本地图片域名 */
    public static final String CONFIG_LOCAL_UPLOAD_URL = "localUploadUrl";
    /** 图片上传,拓展名 */
    public static final String UPLOAD_IMAGE_EXT_STR_CONFIG_KEY = "image_ext_str";
    /** 图片上传,最大尺寸 */
    public static final String UPLOAD_IMAGE_MAX_SIZE_CONFIG_KEY = "image_max_size";
    /** 文件上传,拓展名 */
    public static final String UPLOAD_FILE_EXT_STR_CONFIG_KEY = "file_ext_str";
    /** 文件上传,最大尺寸 */
    public static final String UPLOAD_FILE_MAX_SIZE_CONFIG_KEY = "file_max_size";

    /** 七牛云上传URL */
    public static final String CONFIG_QN_UPLOAD_URL = "qnUploadUrl";
    /** 七牛云Access Key */
    public static final String CONFIG_QN_ACCESS_KEY = "qnAccessKey";
    /** 七牛云Secret Key */
    public static final String CONFIG_QN_SECRET_KEY = "qnSecretKey";
    /** 七牛云存储名称 */
    public static final String CONFIG_QN_STORAGE_NAME = "qnStorageName";
    /** 七牛云存储区域 */
    public static final String CONFIG_QN_STORAGE_REGION = "qnStorageRegion";

    /** 阿里云上传URL */
    public static final String CONFIG_AL_UPLOAD_URL = "alUploadUrl";
    /** 阿里云Access Key */
    public static final String CONFIG_AL_ACCESS_KEY = "alAccessKey";
    /** 阿里云Secret Key */
    public static final String CONFIG_AL_SECRET_KEY = "alSecretKey";
    /** 阿里云存储名称 */
    public static final String CONFIG_AL_STORAGE_NAME = "alStorageName";
    /** 阿里云存储区域 */
    public static final String CONFIG_AL_STORAGE_REGION = "alStorageRegion";

    /** 腾讯云上传URL */
    public static final String CONFIG_TX_UPLOAD_URL = "txUploadUrl";
    /** 腾讯云Access Key */
    public static final String CONFIG_TX_ACCESS_KEY = "txAccessKey";
    /** 腾讯云Secret Key */
    public static final String CONFIG_TX_SECRET_KEY = "txSecretKey";
    /** 腾讯云存储名称 */
    public static final String CONFIG_TX_STORAGE_NAME = "txStorageName";
    /** 腾讯云存储区域 */
    public static final String CONFIG_TX_STORAGE_REGION = "txStorageRegion";

    /** 京东云上传URL */
    public static final String CONFIG_JD_UPLOAD_URL = "jdUploadUrl";
    /** 京东云Access Key */
    public static final String CONFIG_JD_ACCESS_KEY = "jdAccessKey";
    /** 京东云Secret Key */
    public static final String CONFIG_JD_SECRET_KEY = "jdSecretKey";
    /** 京东云存储桶名称 */
    public static final String CONFIG_JD_BUCKET_NAME = "jdBucketName";
    /** 京东云存储区域 */
    public static final String CONFIG_JD_CLOUD_SIGNING_REGION = "jdSigningRegion";
    /** 京东云存储端点 */
    public static final String CONFIG_JD_CLOUD_ENDPOINT = "jdEndpoint";

}
