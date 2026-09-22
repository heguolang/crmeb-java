package com.zbkj.service.service;

import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.response.TeamBrokerageAuditItemResponse;
import com.zbkj.common.response.TeamBrokerageReissueResponse;

import java.util.List;

/**
 * 团队奖分润（极差 + 平级）
 */
public interface TeamBrokerageService {

    /**
     * 按团队等级配置计算并生成团队极差/平级佣金记录
     *
     * @param storeOrder 已支付订单
     * @return 佣金记录列表（可能为空）
     */
    List<UserBrokerageRecord> assignTeamBrokerage(StoreOrder storeOrder);

    /**
     * 漏发补发：按当前配置对指定已支付订单重放团队奖计算，
     * 与已有记录按「用户+奖项」去重后补发缺失部分，并按到账时机配置入账
     *
     * @param orderNo 订单号
     * @return 补发结果（无漏发时 records 为空）
     */
    TeamBrokerageReissueResponse reissueMissingByOrderNo(String orderNo);

    /**
     * 漏发检测：扫描时间范围内已支付且未退款的订单，试算团队奖并比对现有记录（只读）
     *
     * @param startTime 支付开始时间 yyyy-MM-dd
     * @param endTime   支付结束时间 yyyy-MM-dd
     * @param limit     最多扫描订单数（默认 300，上限 1000）
     * @return 存在漏发的订单清单
     */
    List<TeamBrokerageAuditItemResponse> auditMissingTeamBrokerage(String startTime, String endTime, Integer limit);
}
