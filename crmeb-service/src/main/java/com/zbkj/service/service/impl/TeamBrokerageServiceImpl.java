package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.StoreOrderInfoOldVo;
import com.zbkj.service.service.StoreOrderInfoService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.SystemTeamLevelConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import com.zbkj.service.service.TeamBrokerageService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 团队奖分润：沿推荐链向上按团队极差比例递扣分配，平级时按各等级平级奖配置补发
 */
@Service
public class TeamBrokerageServiceImpl implements TeamBrokerageService {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemTeamLevelConfigService systemTeamLevelConfigService;

    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Override
    public List<UserBrokerageRecord> assignTeamBrokerage(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return CollUtil.newArrayList();
        }
        String status = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_STATUS);
        if (StrUtil.isBlank(status) || "0".equals(status)) {
            return CollUtil.newArrayList();
        }
        User buyer = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(buyer)) {
            return CollUtil.newArrayList();
        }
        Integer spreadUid = buyer.getSpreadUid();
        if (ObjectUtil.isNull(spreadUid) || spreadUid <= 0 || spreadUid.equals(storeOrder.getUid())) {
            return CollUtil.newArrayList();
        }

        String frozenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);
        int frozenDays = Integer.parseInt(Optional.ofNullable(frozenTime).orElse("0"));

        BigDecimal accumulatedRate = BigDecimal.ZERO;
        Integer currentUid = spreadUid;
        Set<Integer> visited = new HashSet<>();
        int depth = 0;
        int maxDepth = getMaxDepth();
        List<UserBrokerageRecord> recordList = new ArrayList<>();

        while (ObjectUtil.isNotNull(currentUid) && currentUid > 0) {
            if (!visited.add(currentUid)) {
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                break;
            }

            User upline = userService.getById(currentUid);
            if (ObjectUtil.isNull(upline)) {
                break;
            }

            Integer teamLevelId = ObjectUtil.defaultIfNull(upline.getTeamLevel(), 0);
            if (teamLevelId <= 0) {
                currentUid = upline.getSpreadUid();
                continue;
            }

            SystemTeamLevelConfig config = systemTeamLevelConfigService.getByTeamLevelId(teamLevelId);
            if (ObjectUtil.isNull(config)) {
                currentUid = upline.getSpreadUid();
                continue;
            }

            BigDecimal myRate = new BigDecimal(ObjectUtil.defaultIfNull(config.getTeamBrokerageRate(), 0));
            Integer peerRate = ObjectUtil.defaultIfNull(config.getPeerAwardRate(), 0);
            SystemTeamLevel teamLevel = systemTeamLevelService.getById(teamLevelId);
            String teamLevelName = ObjectUtil.isNotNull(teamLevel) ? teamLevel.getName() : "";

            int compare = myRate.compareTo(accumulatedRate);
            if (compare > 0) {
                BigDecimal diffRate = myRate.subtract(accumulatedRate);
                BigDecimal brokerage = calculateCommissionByRate(storeOrder.getId(), toRateDecimal(diffRate));
                if (brokerage.compareTo(BigDecimal.ZERO) > 0) {
                    recordList.add(buildRecord(upline.getUid(), brokerage, frozenDays,
                            BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_DIFF,
                            BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF,
                            StrUtil.format("获得团队极差奖，团等级【{}】极差{}%，分佣{}",
                                    teamLevelName, diffRate.stripTrailingZeros().toPlainString(), brokerage)));
                }
                accumulatedRate = myRate;
            } else if (compare == 0) {
                if (peerRate > 0) {
                    BigDecimal brokerage = calculateCommissionByRate(storeOrder.getId(), toRateDecimal(new BigDecimal(peerRate)));
                    if (brokerage.compareTo(BigDecimal.ZERO) > 0) {
                        recordList.add(buildRecord(upline.getUid(), brokerage, frozenDays,
                                BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_PEER,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_PEER,
                                StrUtil.format("获得团队平级奖，团等级【{}】平级奖{}%，分佣{}",
                                        teamLevelName, peerRate, brokerage)));
                    }
                }
            }

            currentUid = upline.getSpreadUid();
        }

        return recordList;
    }

    private UserBrokerageRecord buildRecord(Integer uid, BigDecimal brokerage, int frozenDays,
                                            String title, Integer brokerageLevel, String mark) {
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(uid);
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setPrice(brokerage);
        record.setMark(mark);
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
        record.setFrozenTime(frozenDays);
        record.setCreateTime(CrmebDateUtil.nowDateTime());
        record.setBrokerageLevel(brokerageLevel);
        return record;
    }

    private BigDecimal calculateCommissionByRate(Integer orderId, BigDecimal rateDecimal) {
        if (ObjectUtil.isNull(rateDecimal) || rateDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        List<StoreOrderInfoOldVo> orderInfoVoList = storeOrderInfoService.getOrderListByOrderId(orderId);
        if (CollUtil.isEmpty(orderInfoVoList)) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (StoreOrderInfoOldVo orderInfoVo : orderInfoVoList) {
            BigDecimal brokeragePrice;
            if (ObjectUtil.isNotNull(orderInfoVo.getInfo().getVipPrice())) {
                brokeragePrice = orderInfoVo.getInfo().getVipPrice().multiply(rateDecimal).setScale(2, RoundingMode.DOWN);
            } else {
                brokeragePrice = orderInfoVo.getInfo().getPrice().multiply(rateDecimal).setScale(2, RoundingMode.DOWN);
            }
            if (brokeragePrice.compareTo(BigDecimal.ZERO) > 0 && orderInfoVo.getInfo().getPayNum() > 1) {
                brokeragePrice = brokeragePrice.multiply(new BigDecimal(orderInfoVo.getInfo().getPayNum()));
            }
            total = total.add(brokeragePrice);
        }
        return total;
    }

    private BigDecimal toRateDecimal(BigDecimal ratePercent) {
        return ratePercent.divide(new BigDecimal(100), 4, RoundingMode.DOWN);
    }

    private int getMaxDepth() {
        String maxDepthStr = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_MAX_DEPTH);
        try {
            int v = Integer.parseInt(ObjectUtil.defaultIfNull(maxDepthStr, "0"));
            return v <= 0 ? 0 : Math.min(v, 200);
        } catch (Exception e) {
            return 0;
        }
    }
}
