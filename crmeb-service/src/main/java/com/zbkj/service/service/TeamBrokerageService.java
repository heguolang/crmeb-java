package com.zbkj.service.service;

import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.user.UserBrokerageRecord;

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
}
