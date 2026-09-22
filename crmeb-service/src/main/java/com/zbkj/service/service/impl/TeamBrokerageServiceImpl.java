package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.response.TeamBrokerageAuditItemResponse;
import com.zbkj.common.response.TeamBrokerageReissueResponse;
import com.zbkj.common.utils.BrokeragePriceUtil;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.vo.OrderInfoDetailVo;
import com.zbkj.common.vo.StoreOrderInfoOldVo;
import com.zbkj.service.service.StoreOrderInfoService;
import com.zbkj.service.service.StoreOrderService;
import com.zbkj.service.service.StoreOrderStatusService;
import com.zbkj.service.service.StoreProductAttrValueService;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.SystemTeamLevelConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import com.zbkj.service.service.TeamBrokerageService;
import com.zbkj.service.service.UserBrokerageRecordService;
import com.zbkj.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 团队奖分润：沿推荐链向上按团队极差比例递扣分配，平级时按各等级平级奖配置补发
 */
@Service
public class TeamBrokerageServiceImpl implements TeamBrokerageService {

    private static final Logger logger = LoggerFactory.getLogger(TeamBrokerageServiceImpl.class);

    /** 订单状态日志类型，便于按订单排查团队奖 */
    private static final String ORDER_LOG_TEAM_BROKERAGE = "team_brokerage";

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

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreProductAttrValueService storeProductAttrValueService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public List<UserBrokerageRecord> assignTeamBrokerage(StoreOrder storeOrder) {
        return computeTeamBrokerage(storeOrder, true);
    }

    /**
     * 团队奖计算（writeTrace=false 时不写订单日志，供后台漏发检测批量试算使用）
     */
    private List<UserBrokerageRecord> computeTeamBrokerage(StoreOrder storeOrder, boolean writeTrace) {
        List<String> trace = new ArrayList<>();
        if (ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return CollUtil.newArrayList();
        }
        String orderNo = storeOrder.getOrderId();
        Integer orderId = storeOrder.getId();
        trace.add(StrUtil.format("orderNo={},orderId={},buyerUid={},payPrice={}",
                orderNo, orderId, storeOrder.getUid(), storeOrder.getPayPrice()));

        String status = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_STATUS);
        trace.add(StrUtil.format("team_brokerage_status={}", status));
        if (StrUtil.isBlank(status) || "0".equals(status)) {
            return finishEmpty(storeOrder, trace, "团队奖总开关关闭或未配置", writeTrace);
        }
        User buyer = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(buyer)) {
            return finishEmpty(storeOrder, trace, "买家不存在", writeTrace);
        }
        Integer spreadUid = buyer.getSpreadUid();
        trace.add(StrUtil.format("buyerTeamLevel={},spreadUid={}", buyer.getTeamLevel(), spreadUid));
        if (ObjectUtil.isNull(spreadUid) || spreadUid <= 0 || spreadUid.equals(storeOrder.getUid())) {
            return finishEmpty(storeOrder, trace, "无有效上级", writeTrace);
        }

        // 商品参与情况快照（便于判断 is_team_brokerage）
        trace.add(buildProductSnapshot(orderId));

        String frozenTime = systemConfigService.getValueByKey(Constants.CONFIG_KEY_STORE_BROKERAGE_EXTRACT_TIME);
        int frozenDays = Integer.parseInt(Optional.ofNullable(frozenTime).orElse("0"));

        BigDecimal accumulatedRate = BigDecimal.ZERO;
        Integer currentUid = spreadUid;
        Set<Integer> visited = new HashSet<>();
        int depth = 0;
        int maxDepth = getMaxDepth();
        List<UserBrokerageRecord> recordList = new ArrayList<>();
        trace.add(StrUtil.format("maxDepth={}", maxDepth <= 0 ? "不限" : maxDepth));

        while (ObjectUtil.isNotNull(currentUid) && currentUid > 0) {
            if (!visited.add(currentUid)) {
                trace.add(StrUtil.format("depth停:环路uid={}", currentUid));
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                trace.add(StrUtil.format("depth停:超过maxDepth={}", maxDepth));
                break;
            }

            User upline = userService.getById(currentUid);
            if (ObjectUtil.isNull(upline)) {
                trace.add(StrUtil.format("depth{}停:上级uid={}不存在", depth, currentUid));
                break;
            }

            Integer teamLevelId = ObjectUtil.defaultIfNull(upline.getTeamLevel(), 0);
            if (teamLevelId <= 0) {
                trace.add(StrUtil.format("depth{}:uid={}无团等级,跳过", depth, currentUid));
                currentUid = upline.getSpreadUid();
                continue;
            }

            SystemTeamLevelConfig config = systemTeamLevelConfigService.getByTeamLevelId(teamLevelId);
            if (ObjectUtil.isNull(config)) {
                trace.add(StrUtil.format("depth{}:uid={}团等级id={}无配置,跳过", depth, currentUid, teamLevelId));
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
                CommissionCalcResult calc = calculateCommissionByRateDetail(storeOrder.getId(), toRateDecimal(diffRate));
                if (calc.amount.compareTo(BigDecimal.ZERO) > 0) {
                    recordList.add(buildRecord(upline.getUid(), calc.amount, frozenDays,
                            BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_DIFF,
                            BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF,
                            StrUtil.format("获得团队极差奖，团等级【{}】极差{}%，分佣{}",
                                    teamLevelName, diffRate.stripTrailingZeros().toPlainString(), calc.amount)));
                    trace.add(StrUtil.format("depth{}:uid={}等级{}({})极差率{}%(累计前{}),金额{},{}",
                            depth, currentUid, teamLevelName, teamLevelId,
                            diffRate.stripTrailingZeros().toPlainString(),
                            accumulatedRate.stripTrailingZeros().toPlainString(),
                            calc.amount, calc.detail));
                } else {
                    trace.add(StrUtil.format("depth{}:uid={}等级{}({})应极差{}%但金额=0,原因:{}",
                            depth, currentUid, teamLevelName, teamLevelId,
                            diffRate.stripTrailingZeros().toPlainString(), calc.detail));
                }
                accumulatedRate = myRate;
            } else if (compare == 0) {
                if (peerRate > 0) {
                    CommissionCalcResult calc = calculateCommissionByRateDetail(storeOrder.getId(),
                            toRateDecimal(new BigDecimal(peerRate)));
                    if (calc.amount.compareTo(BigDecimal.ZERO) > 0) {
                        recordList.add(buildRecord(upline.getUid(), calc.amount, frozenDays,
                                BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_TEAM_PEER,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_PEER,
                                StrUtil.format("获得团队平级奖，团等级【{}】平级奖{}%，分佣{}",
                                        teamLevelName, peerRate, calc.amount)));
                        trace.add(StrUtil.format("depth{}:uid={}等级{}({})平级{}%,金额{},{}",
                                depth, currentUid, teamLevelName, teamLevelId, peerRate, calc.amount, calc.detail));
                    } else {
                        trace.add(StrUtil.format("depth{}:uid={}等级{}({})平级{}%但金额=0,原因:{}",
                                depth, currentUid, teamLevelName, teamLevelId, peerRate, calc.detail));
                    }
                } else {
                    trace.add(StrUtil.format("depth{}:uid={}等级{}({})与累计比例{}%相同但平级奖比例=0,跳过",
                            depth, currentUid, teamLevelName, teamLevelId,
                            accumulatedRate.stripTrailingZeros().toPlainString()));
                }
            } else {
                trace.add(StrUtil.format("depth{}:uid={}等级{}({})比例{}%<累计{}%,不发",
                        depth, currentUid, teamLevelName, teamLevelId,
                        myRate.stripTrailingZeros().toPlainString(),
                        accumulatedRate.stripTrailingZeros().toPlainString()));
            }

            currentUid = upline.getSpreadUid();
        }

        finishTrace(storeOrder, trace, recordList, null, writeTrace);
        return recordList;
    }

    /**
     * 漏发补发：按当前配置重放计算，与已有记录按「用户+奖项」去重后补发缺失部分
     */
    @Override
    public TeamBrokerageReissueResponse reissueMissingByOrderNo(String orderNo) {
        TeamBrokerageReissueResponse response = new TeamBrokerageReissueResponse()
                .setRecords(CollUtil.newArrayList())
                .setExistingCount(0)
                .setInvalidCount(0)
                .setTotalAmount(BigDecimal.ZERO);
        if (StrUtil.isBlank(orderNo)) {
            throw new CrmebException("订单号不能为空");
        }
        StoreOrder storeOrder = storeOrderService.getByOderId(orderNo.trim());
        if (ObjectUtil.isNull(storeOrder)) {
            throw new CrmebException("订单不存在：" + orderNo);
        }
        if (!Boolean.TRUE.equals(storeOrder.getPaid())) {
            throw new CrmebException("订单未支付，无法补发团队奖");
        }
        if (ObjectUtil.isNotNull(storeOrder.getRefundStatus()) && storeOrder.getRefundStatus() > 0) {
            throw new CrmebException(StrUtil.format("订单已退款或退款中（refundStatus={}），不能补发团队奖",
                    storeOrder.getRefundStatus()));
        }

        // 已有团队奖记录（含历史/手工补发），按 用户uid+奖项等级 去重（失效记录同样占位，避免重复发放）
        List<UserBrokerageRecord> existingList = userBrokerageRecordService.list(
                new LambdaQueryWrapper<UserBrokerageRecord>()
                        .eq(UserBrokerageRecord::getLinkId, storeOrder.getOrderId())
                        .eq(UserBrokerageRecord::getLinkType, BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER)
                        .eq(UserBrokerageRecord::getType, BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD)
                        .in(UserBrokerageRecord::getBrokerageLevel,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_PEER));
        Set<String> existingKeys = existingList.stream()
                .map(r -> r.getUid() + "_" + r.getBrokerageLevel())
                .collect(Collectors.toSet());
        response.setExistingCount(existingList.size());
        response.setInvalidCount((int) existingList.stream()
                .filter(r -> BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION.equals(r.getStatus()))
                .count());

        // 按当前等级/配置重放计算
        List<UserBrokerageRecord> replayList = computeTeamBrokerage(storeOrder, false);
        List<UserBrokerageRecord> missingList = replayList.stream()
                .filter(r -> !existingKeys.contains(r.getUid() + "_" + r.getBrokerageLevel()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(missingList)) {
            return response;
        }
        missingList.forEach(r -> r.setMark(StrUtil.format("{}（后台补发）", StrUtil.nullToEmpty(r.getMark()))));

        // 到账时机：支付即到账则直接入账，否则保持创建状态等待订单完成入账
        boolean onPay = isPayCreditTiming();
        final Map<Integer, BigDecimal> balanceCursor = new HashMap<>();
        if (onPay) {
            for (UserBrokerageRecord r : missingList) {
                BigDecimal before = balanceCursor.computeIfAbsent(r.getUid(), uid -> {
                    User u = userService.getById(uid);
                    return ObjectUtil.isNotNull(u)
                            ? ObjectUtil.defaultIfNull(u.getBrokeragePrice(), BigDecimal.ZERO)
                            : BigDecimal.ZERO;
                });
                r.setBalance(before.add(ObjectUtil.defaultIfNull(r.getPrice(), BigDecimal.ZERO)));
                r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                r.setFrozenTime(0);
                balanceCursor.put(r.getUid(), r.getBalance());
            }
        }

        Boolean execute = transactionTemplate.execute(e -> {
            missingList.forEach(r -> r.setLinkId(storeOrder.getOrderId()));
            userBrokerageRecordService.saveBatch(missingList);
            if (onPay) {
                for (UserBrokerageRecord r : missingList) {
                    Boolean ok = userService.operationBrokerage(r.getUid(), r.getPrice(),
                            balanceCursor.get(r.getUid()).subtract(ObjectUtil.defaultIfNull(r.getPrice(), BigDecimal.ZERO)), "add");
                    if (!Boolean.TRUE.equals(ok)) {
                        throw new CrmebException("团队奖补发入账失败 uid=" + r.getUid());
                    }
                }
            }
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(execute)) {
            throw new CrmebException("团队奖补发事务执行失败");
        }

        BigDecimal total = missingList.stream()
                .map(UserBrokerageRecord::getPrice)
                .filter(ObjectUtil::isNotNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String msg = StrUtil.format("[TeamBrokerage] 后台手工补发 orderNo={}, 补发{}条, 合计{}, 到账方式={}",
                storeOrder.getOrderId(), missingList.size(), total, onPay ? "支付即到账" : "订单完成到账");
        logger.info(msg);
        try {
            storeOrderStatusService.createLog(storeOrder.getId(), ORDER_LOG_TEAM_BROKERAGE,
                    msg.length() > 240 ? msg.substring(0, 240) + "..." : msg);
        } catch (Exception e) {
            logger.warn("[TeamBrokerage] 补发写订单日志失败 orderNo={}", storeOrder.getOrderId(), e);
        }
        return response.setRecords(missingList).setTotalAmount(total);
    }

    /**
     * 漏发检测：扫描时间范围内已支付且未退款的订单，试算团队奖并与现有记录比对，
     * 返回存在漏发的订单清单（只读，不写库）
     */
    @Override
    public List<TeamBrokerageAuditItemResponse> auditMissingTeamBrokerage(String startTime, String endTime, Integer limit) {
        int max = ObjectUtil.defaultIfNull(limit, 300);
        max = max <= 0 ? 300 : Math.min(max, 1000);
        Date start = StrUtil.isBlank(startTime)
                ? cn.hutool.core.date.DateUtil.offsetDay(new Date(), -30)
                : cn.hutool.core.date.DateUtil.parse(startTime.trim() + " 00:00:00");
        Date end = StrUtil.isBlank(endTime)
                ? new Date()
                : cn.hutool.core.date.DateUtil.parse(endTime.trim() + " 23:59:59");

        LambdaQueryWrapper<StoreOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(StoreOrder::getPaid, true);
        orderWrapper.and(w -> w.isNull(StoreOrder::getRefundStatus).or().eq(StoreOrder::getRefundStatus, 0));
        orderWrapper.between(StoreOrder::getPayTime, start, end);
        orderWrapper.orderByDesc(StoreOrder::getPayTime, StoreOrder::getId);
        orderWrapper.last("limit " + max);
        List<StoreOrder> orderList = storeOrderService.list(orderWrapper);
        if (CollUtil.isEmpty(orderList)) {
            return CollUtil.newArrayList();
        }

        // 批量取现有团队奖记录，按 订单号 -> (uid_奖项) 建索引
        List<String> orderNos = orderList.stream().map(StoreOrder::getOrderId).collect(Collectors.toList());
        List<UserBrokerageRecord> existingAll = userBrokerageRecordService.list(
                new LambdaQueryWrapper<UserBrokerageRecord>()
                        .in(UserBrokerageRecord::getLinkId, orderNos)
                        .eq(UserBrokerageRecord::getLinkType, BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER)
                        .eq(UserBrokerageRecord::getType, BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD)
                        .in(UserBrokerageRecord::getBrokerageLevel,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF,
                                BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_PEER));
        Map<String, Set<String>> existingMap = existingAll.stream().collect(Collectors.groupingBy(
                UserBrokerageRecord::getLinkId,
                Collectors.mapping(r -> r.getUid() + "_" + r.getBrokerageLevel(), Collectors.toSet())));

        List<TeamBrokerageAuditItemResponse> resultList = CollUtil.newArrayList();
        for (StoreOrder order : orderList) {
            List<UserBrokerageRecord> expectedList = computeTeamBrokerage(order, false);
            if (CollUtil.isEmpty(expectedList)) {
                continue;
            }
            Set<String> existingKeys = existingMap.getOrDefault(order.getOrderId(), new HashSet<>());
            List<UserBrokerageRecord> missingList = expectedList.stream()
                    .filter(r -> !existingKeys.contains(r.getUid() + "_" + r.getBrokerageLevel()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(missingList)) {
                continue;
            }
            BigDecimal missingAmount = missingList.stream()
                    .map(UserBrokerageRecord::getPrice)
                    .filter(ObjectUtil::isNotNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            String detail = missingList.stream()
                    .map(r -> StrUtil.format("{} {}元",
                            BrokerageRecordConstants.BROKERAGE_LEVEL_TEAM_DIFF.equals(r.getBrokerageLevel()) ? "极差奖" : "平级奖",
                            ObjectUtil.defaultIfNull(r.getPrice(), BigDecimal.ZERO)))
                    .collect(Collectors.joining("、"));
            resultList.add(new TeamBrokerageAuditItemResponse()
                    .setOrderId(order.getId())
                    .setOrderNo(order.getOrderId())
                    .setUid(order.getUid())
                    .setPayPrice(order.getPayPrice())
                    .setPayTime(order.getPayTime())
                    .setMissingCount(missingList.size())
                    .setMissingAmount(missingAmount)
                    .setMissingDetail(detail));
        }
        logger.info("[TeamBrokerage] 漏发检测 时间{}~{} 扫描{}单, 命中漏发{}单", start, end, orderList.size(), resultList.size());
        return resultList;
    }

    /**
     * 团队奖是否支付即到账（默认是）
     */
    private boolean isPayCreditTiming() {
        String value = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_CREDIT_TIMING);
        return StrUtil.isBlank(value) || SysConfigConstants.CREDIT_TIMING_ON_PAY.equals(value);
    }

    private List<UserBrokerageRecord> finishEmpty(StoreOrder storeOrder, List<String> trace, String reason, boolean writeTrace) {
        finishTrace(storeOrder, trace, CollUtil.newArrayList(), reason, writeTrace);
        return CollUtil.newArrayList();
    }

    private void finishTrace(StoreOrder storeOrder, List<String> trace, List<UserBrokerageRecord> recordList,
                             String earlyReason, boolean writeTrace) {
        if (StrUtil.isNotBlank(earlyReason)) {
            trace.add("结果:" + earlyReason);
        } else {
            BigDecimal total = recordList.stream()
                    .map(UserBrokerageRecord::getPrice)
                    .filter(ObjectUtil::isNotNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trace.add(StrUtil.format("结果:生成{}条,合计{}", recordList.size(), total));
        }
        String full = "[TeamBrokerage] " + String.join(" | ", trace);
        logger.info(full);
        if (!writeTrace) {
            return;
        }

        // 写入订单日志（截断，避免超出 change_message 长度）
        try {
            String shortMsg = full.length() > 240 ? full.substring(0, 240) + "..." : full;
            storeOrderStatusService.createLog(storeOrder.getId(), ORDER_LOG_TEAM_BROKERAGE, shortMsg);
        } catch (Exception e) {
            logger.warn("[TeamBrokerage] 写订单日志失败 orderNo={}", storeOrder.getOrderId(), e);
        }
    }

    private String buildProductSnapshot(Integer orderId) {
        List<StoreOrderInfoOldVo> orderInfoVoList = storeOrderInfoService.getOrderListByOrderId(orderId);
        if (CollUtil.isEmpty(orderInfoVoList)) {
            return "商品明细为空";
        }
        List<Integer> productIds = orderInfoVoList.stream()
                .map(StoreOrderInfoOldVo::getProductId)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, StoreProduct> productMap = CollUtil.isEmpty(productIds)
                ? new HashMap<>()
                : storeProductService.getListInIds(productIds).stream()
                .collect(Collectors.toMap(StoreProduct::getId, p -> p, (a, b) -> a));
        List<String> parts = new ArrayList<>();
        for (StoreOrderInfoOldVo vo : orderInfoVoList) {
            StoreProduct product = productMap.get(vo.getProductId());
            OrderInfoDetailVo info = vo.getInfo();
            Boolean isTeam = ObjectUtil.isNull(product) ? null : product.getIsTeamBrokerage();
            BigDecimal sell = BigDecimal.ZERO;
            BigDecimal cost = BigDecimal.ZERO;
            if (ObjectUtil.isNotNull(info)) {
                sell = ObjectUtil.defaultIfNull(
                        ObjectUtil.isNotNull(info.getVipPrice()) ? info.getVipPrice() : info.getPrice(),
                        BigDecimal.ZERO);
                cost = ObjectUtil.defaultIfNull(info.getCost(), BigDecimal.ZERO);
            }
            parts.add(StrUtil.format("pid={} isTeamBrokerage={} sell={} cost={} base={}",
                    vo.getProductId(), isTeam, sell, cost, sell.subtract(cost).max(BigDecimal.ZERO)));
        }
        return "商品[" + String.join("; ", parts) + "]";
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

    private static class CommissionCalcResult {
        private final BigDecimal amount;
        private final String detail;

        private CommissionCalcResult(BigDecimal amount, String detail) {
            this.amount = amount;
            this.detail = detail;
        }
    }

    private CommissionCalcResult calculateCommissionByRateDetail(Integer orderId, BigDecimal rateDecimal) {
        if (ObjectUtil.isNull(rateDecimal) || rateDecimal.compareTo(BigDecimal.ZERO) <= 0) {
            return new CommissionCalcResult(BigDecimal.ZERO, "比例<=0");
        }
        List<StoreOrderInfoOldVo> orderInfoVoList = storeOrderInfoService.getOrderListByOrderId(orderId);
        if (CollUtil.isEmpty(orderInfoVoList)) {
            return new CommissionCalcResult(BigDecimal.ZERO, "订单明细为空");
        }
        List<Integer> productIds = orderInfoVoList.stream()
                .map(StoreOrderInfoOldVo::getProductId)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, StoreProduct> productMap = CollUtil.isEmpty(productIds)
                ? new HashMap<>()
                : storeProductService.getListInIds(productIds).stream()
                .collect(Collectors.toMap(StoreProduct::getId, p -> p, (a, b) -> a));
        List<Integer> attrValueIds = orderInfoVoList.stream()
                .map(vo -> ObjectUtil.isNotNull(vo.getInfo()) ? vo.getInfo().getAttrValueId() : null)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, StoreProductAttrValue> attrValueMap = CollUtil.isEmpty(attrValueIds)
                ? new HashMap<>()
                : storeProductAttrValueService.listByIds(attrValueIds).stream()
                .collect(Collectors.toMap(StoreProductAttrValue::getId, a -> a, (a, b) -> a));
        BigDecimal total = BigDecimal.ZERO;
        List<String> lineDetails = new ArrayList<>();
        for (StoreOrderInfoOldVo orderInfoVo : orderInfoVoList) {
            StoreProduct product = productMap.get(orderInfoVo.getProductId());
            // 未配置或默认参与；显式关闭则跳过
            if (ObjectUtil.isNotNull(product) && Boolean.FALSE.equals(product.getIsTeamBrokerage())) {
                lineDetails.add(StrUtil.format("pid{}关闭团队奖", orderInfoVo.getProductId()));
                continue;
            }
            OrderInfoDetailVo info = orderInfoVo.getInfo();
            if (ObjectUtil.isNull(info)) {
                lineDetails.add(StrUtil.format("pid{}无info", orderInfoVo.getProductId()));
                continue;
            }
            StoreProductAttrValue attrValue = attrValueMap.get(info.getAttrValueId());
            BigDecimal unitBase = BrokeragePriceUtil.resolveUnitBase(info, product, attrValue);
            BigDecimal brokeragePrice = BrokeragePriceUtil.calcLineBrokerage(info, product, attrValue, rateDecimal);
            lineDetails.add(StrUtil.format("pid{} base={} line={}", orderInfoVo.getProductId(), unitBase, brokeragePrice));
            total = total.add(brokeragePrice);
        }
        return new CommissionCalcResult(total, String.join(",", lineDetails));
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
