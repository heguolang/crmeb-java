package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.FundsMonitorRequest;
import com.zbkj.common.request.FundsMonitorSearchRequest;
import com.zbkj.common.response.MonitorResponse;
import com.zbkj.common.request.StoreOrderRefundRequest;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBill;
import com.zbkj.common.model.order.StoreOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * UserBillService 接口实现
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
public interface UserBillService extends IService<UserBill> {

    /**
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<UserBill>
     */
    List<UserBill> getList(FundsMonitorSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增/消耗  总金额
     *
     * @param pm       Integer 0 = 支出 1 = 获得
     * @param userId   Integer 用户uid
     * @param category String 类型
     * @param date     String 时间范围
     * @param type     String 小类型
     * @return UserBill
     */
    BigDecimal getSumBigDecimal(Integer pm, Integer userId, String category, String date, String type);

    /**
     * 保存退款日志
     *
     * @return boolean
     */
    Boolean saveRefundBill(StoreOrderRefundRequest request, User user);

    /**
     * 保存退款日志（指定备注，用于微信/支付宝原路退回）
     *
     * @param order 订单
     * @param user  用户
     * @param amount 退款金额
     * @param mark  备注
     * @return boolean
     */
    Boolean saveRefundBill(StoreOrder order, User user, BigDecimal amount, String mark);

    /**
     * 资金监控——余额/佣金账单表
     *
     * @param request 查询参数（含分页）
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoring(FundsMonitorRequest request);

    /**
     * 资金监控——全部账户（余额+信用值+佣金 三表 UNION）
     *
     * @param request 查询参数（含分页）
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringAll(FundsMonitorRequest request);

    /**
     * 资金监控——信用值账户
     *
     * @param request 查询参数（含分页）
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringIntegral(FundsMonitorRequest request);

    /**
     * 资金监控——佣金账户
     *
     * @param request 查询参数（含分页）
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringBrokerage(FundsMonitorRequest request);

    /**
     * 用户账单记录（现金）
     *
     * @param uid  用户uid
     * @param type 记录类型：all-全部，expenditure-支出，income-收入
     * @return PageInfo
     */
    PageInfo<UserBill> nowMoneyBillRecord(Integer uid, String type, PageParamRequest pageRequest);
}
