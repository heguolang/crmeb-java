package com.zbkj.common.constants;

/**
 * 用户常量表
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
public class UserConstants {

    /** 用户类型——H5 */
    public static final String USER_TYPE_H5 = "h5";
    /** 用户类型——公众号 */
    public static final String USER_TYPE_WECHAT = "wechat";
    /** 用户类型——小程序 */
    public static final String USER_TYPE_ROUTINE = "routine";

    /**
     * =========================================================
     * UserToken部分
     * =========================================================
     */
    /** 用户Token类型——公众号 */
    public static final Integer USER_TOKEN_TYPE_WECHAT = 1;
    /** 用户Token类型——小程序 */
    public static final Integer USER_TOKEN_TYPE_ROUTINE = 2;
    /** 用户Token类型——unionid */
    public static final Integer USER_TOKEN_TYPE_UNIONID = 3;

    /**
     * =========================================================
     * 用户公共搜索（资金监控等页面）
     * =========================================================
     */
    /** 搜索类型——全部 */
    public static final String USER_SEARCH_TYPE_ALL = "all";
    /** 搜索类型——UID */
    public static final String USER_SEARCH_TYPE_UID = "uid";
    /** 搜索类型——用户昵称 */
    public static final String USER_SEARCH_TYPE_NICKNAME = "nickname";
    /** 搜索类型——手机号 */
    public static final String USER_SEARCH_TYPE_PHONE = "phone";
}
