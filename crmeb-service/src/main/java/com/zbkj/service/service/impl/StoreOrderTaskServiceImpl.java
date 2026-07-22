package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.*;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.sms.SmsTemplate;
import com.zbkj.common.model.system.SystemNotification;
import com.zbkj.common.model.user.*;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.model.bargain.StoreBargain;
import com.zbkj.common.model.combination.StoreCombination;
import com.zbkj.common.model.combination.StorePink;
import com.zbkj.common.model.coupon.StoreCouponUser;
import com.zbkj.common.model.seckill.StoreSeckill;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.order.StoreOrderInfo;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.system.SystemAdmin;
import com.zbkj.common.utils.RedisUtil;
import com.zbkj.service.delete.OrderUtils;
import com.zbkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StoreOrderTaskService实现类
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
@Service
public class StoreOrderTaskServiceImpl implements StoreOrderTaskService {
    //日志
    private static final Logger logger = LoggerFactory.getLogger(StoreOrderTaskServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StoreSeckillService storeSeckillService;

    @Autowired
    private StoreBargainService storeBargainService;

    @Autowired
    private StoreCombinationService storeCombinationService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SystemAdminService systemAdminService;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private TemplateMessageService templateMessageService;

    @Autowired
    private OrderUtils orderUtils;

    @Autowired
    private StorePinkService storePinkService;

    @Autowired
    private UserIntegralRecordService userIntegralRecordService;

    @Autowired
    private UserLevelService userLevelService;

    @Autowired
    private UserTeamLevelService userTeamLevelService;

    @Autowired
    private SystemUserLevelService systemUserLevelService;

    @Autowired
    private StoreProductAttrValueService attrValueService;

    @Autowired
    private StoreCouponUserService couponUserService;

    @Autowired
    private UserExperienceRecordService userExperienceRecordService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    @Autowired
    private SmsTemplateService smsTemplateService;

    /**
     * 用户取消订单
     * @author Mr.Zhang
     * @since 2020-07-09
     */
    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CrmebException.class})
    public Boolean cancelByUser(StoreOrder storeOrder) {
        try{
            /*
            * 1、修改订单状态 （用户操作的时候已处理）
            * 2、写订单日志
            * 3、回滚库存
            * 4、回滚优惠券
            * 5、回滚积分
            * */

            Boolean execute = transactionTemplate.execute(e -> {
                //写订单日志
                storeOrderStatusService.createLog(storeOrder.getId(), "cancel_order", "取消订单");
                // 退优惠券
                if (storeOrder.getCouponId() > 0) {
                    StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                    couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                    couponUser.setUpdateTime(DateUtil.date());
                    couponUserService.updateById(couponUser);
                }
                Boolean rollbackStock = rollbackStock(storeOrder);
                if (!rollbackStock) {
                    throw new CrmebException("回滚库存失败");
                }
                return Boolean.TRUE;
            });
            return execute;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 完成订单
     * @author Mr.Zhang
     * @since 2020-07-09
     */
    @Override
    @Transactional(rollbackFor = {RuntimeException.class, Error.class, CrmebException.class})
    public Boolean complete(StoreOrder storeOrder) {
        /*
         * 1、修改订单状态 （用户操作的时候已处理）
         * 2、写订单日志
         * 3、交易完成时累计消费金额并触发升级
         * */
        try{
            storeOrderStatusService.createLog(storeOrder.getId(), "check_order_over", "用户评价");
            userLevelService.processLevelOnOrderComplete(storeOrder);
            userTeamLevelService.processTeamLevelOnOrderComplete(storeOrder);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 回滚库存
     * @param storeOrder 订单信息
     */
    private Boolean rollbackStock(StoreOrder storeOrder) {
        try{
            // 查找出商品详情
            List<StoreOrderInfo> orderInfoList = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
            if(null == orderInfoList || orderInfoList.size() < 1){
                return true;
            }

            // 兼容处理秒杀数据退款
            // 秒杀商品回滚库存和销量
            if(null != storeOrder.getSeckillId() && storeOrder.getSeckillId() > 0){
                // 秒杀只会有一个商品
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreSeckill storeSeckill = storeSeckillService.getById(storeOrder.getSeckillId());
                storeSeckillService.operationStock(storeOrder.getSeckillId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue seckillAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeSeckill.getId(), Constants.PRODUCT_TYPE_SECKILL);
                attrValueService.operationStock(seckillAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_SECKILL, seckillAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeSeckill.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeSeckill.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            } else if (ObjectUtil.isNotNull(storeOrder.getBargainId()) && storeOrder.getBargainId() > 0) { // 砍价商品回滚销量库存
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreBargain storeBargain = storeBargainService.getById(storeOrder.getBargainId());
                storeBargainService.operationStock(storeBargain.getId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue bargainAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeBargain.getId(), Constants.PRODUCT_TYPE_BARGAIN);
                attrValueService.operationStock(bargainAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_BARGAIN, bargainAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeBargain.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeBargain.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            } else if (ObjectUtil.isNotNull(storeOrder.getCombinationId()) && storeOrder.getCombinationId() > 0) { // 拼团商品回滚销量库存
                StoreOrderInfo orderInfo = orderInfoList.get(0);
                StoreCombination storeCombination = storeCombinationService.getById(storeOrder.getCombinationId());
                storeCombinationService.operationStock(storeCombination.getId(), orderInfo.getPayNum(), "add");
                StoreProductAttrValue combinationAttrValue = attrValueService.getByIdAndProductIdAndType(orderInfo.getAttrValueId(), storeCombination.getId(), Constants.PRODUCT_TYPE_PINGTUAN);
                attrValueService.operationStock(combinationAttrValue.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_PINGTUAN, combinationAttrValue.getVersion());
                StoreProduct storeProduct = storeProductService.getById(storeCombination.getProductId());
                storeProductService.operationStock(storeProduct.getId(), orderInfo.getPayNum(), "add", storeProduct.getVersion());
                List<StoreProductAttrValue> attrValueList = attrValueService.getListByProductIdAndType(storeCombination.getProductId(), Constants.PRODUCT_TYPE_NORMAL);
                attrValueList.forEach(e -> {
                    if (e.getSuk().equals(orderInfo.getSku())) {
                        attrValueService.operationStock(e.getId(), orderInfo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, e.getVersion());
                    }
                });
            }
            else { // 正常商品回滚销量库存
                for (StoreOrderInfo orderInfoVo : orderInfoList) {
                    StoreProduct storeProduct = storeProductService.getById(orderInfoVo.getProductId());
                    storeProductService.operationStock(storeProduct.getId(), orderInfoVo.getPayNum(), "add", storeProduct.getVersion());
                    StoreProductAttrValue productAttrValue = attrValueService.getById(orderInfoVo.getAttrValueId());
                    attrValueService.operationStock(productAttrValue.getId(), orderInfoVo.getPayNum(), "add", Constants.PRODUCT_TYPE_NORMAL, productAttrValue.getVersion());
                }
            }
        } catch (Exception e) {
            logger.error("回滚库存失败，error = " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 订单退款处理
     * 退款得时候根据userBill 来进行回滚
     */
    @Override
    public Boolean refundOrder(StoreOrder storeOrder) {
        /**
         * 1、写订单日志
         * 2、回滚消耗积分
         * 3、回滚获得积分
         * 4、回滚冻结期佣金
         * 5、回滚经验
         * 6、回滚库存
         * 7、发送通知
         * 实际上2-5就是user数据的处理+userBill的记录
         */
        // 获取用户对象
        User user = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(user)) {
            logger.error("订单退款处理，对应的用户不存在,storeOrder===>" + storeOrder);
            return Boolean.FALSE;
        }

        // 回滚经验（关闭经验升级时跳过）
        final UserExperienceRecord refundExperienceRecord;
        if (UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            UserExperienceRecord userExperienceRecord = userExperienceRecordService.getByOrderNoAndUid(storeOrder.getOrderId(), storeOrder.getUid());
            if (ObjectUtil.isNotNull(userExperienceRecord)) {
                user.setExperience(user.getExperience() - userExperienceRecord.getExperience());
                refundExperienceRecord = new UserExperienceRecord();
                BeanUtils.copyProperties(userExperienceRecord, refundExperienceRecord);
                refundExperienceRecord.setId(null);
                refundExperienceRecord.setTitle(ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_REFUND);
                refundExperienceRecord.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_SUB);
                refundExperienceRecord.setBalance(user.getExperience());
                refundExperienceRecord.setMark(StrUtil.format("订单退款，扣除{}赠送经验", userExperienceRecord.getExperience()));
                refundExperienceRecord.setCreateTime(cn.hutool.core.date.DateUtil.date());
            } else {
                refundExperienceRecord = null;
            }
        } else {
            refundExperienceRecord = null;
        }

        // 回滚订单数（仅当该订单已计入订单数时）
        final UserExperienceRecord refundOrderCountRecord;
        boolean orderCountChanged = false;
        UserExperienceRecord orderCountRecord = userExperienceRecordService.getByOrderNoAndUidAndLinkType(
                storeOrder.getOrderId(), storeOrder.getUid(),
                ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER_COUNT);
        if (ObjectUtil.isNotNull(orderCountRecord)) {
            refundOrderCountRecord = new UserExperienceRecord();
            BeanUtils.copyProperties(orderCountRecord, refundOrderCountRecord);
            refundOrderCountRecord.setId(null);
            refundOrderCountRecord.setTitle(ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_REFUND);
            refundOrderCountRecord.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_SUB);
            refundOrderCountRecord.setBalance(Math.max(0,
                    ObjectUtil.defaultIfNull(userExperienceRecordService.countCompleteOrderByUid(storeOrder.getUid()), 0) - 1));
            refundOrderCountRecord.setMark("订单退款，扣除1笔交易完成计单");
            refundOrderCountRecord.setCreateTime(cn.hutool.core.date.DateUtil.date());
            orderCountChanged = true;
        } else {
            refundOrderCountRecord = null;
        }
        if (Boolean.TRUE.equals(systemUserLevelService.hasOrderCountTriggerOnPaid())
                && Boolean.TRUE.equals(storeOrder.getPaid())) {
            user.setPayCount(Math.max(0, ObjectUtil.defaultIfNull(user.getPayCount(), 0) - 1));
            orderCountChanged = true;
        }
        final boolean levelStatsChanged = ObjectUtil.isNotNull(refundExperienceRecord) || orderCountChanged;

        // 回滚积分
        List<UserIntegralRecord> integralRecordList = userIntegralRecordService.findListByOrderIdAndUid(storeOrder.getOrderId(), storeOrder.getUid());
        List<UserIntegralRecord> clawbackIntegralList = CollUtil.newArrayList();
        integralRecordList.forEach(record -> {
            if (record.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_SUB)) {// 订单抵扣部分
                user.setIntegral(ObjectUtil.defaultIfNull(user.getIntegral(), BigDecimal.ZERO).add(record.getIntegral()));
                record.setId(null);
                record.setTitle(IntegralRecordConstants.BROKERAGE_RECORD_TITLE_REFUND);
                record.setType(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD);
                record.setBalance(user.getIntegral());
                record.setMark(StrUtil.format("订单退款，返还支付扣除得{}积分", record.getIntegral()));
                record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
                record.setUpdateTime(cn.hutool.core.date.DateUtil.date());
            } else if (record.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD)) {// 赠送积分
                if (record.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE)) {
                    clawbackIntegralList.add(record);
                }
                record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_INVALIDATION);
                record.setUpdateTime(cn.hutool.core.date.DateUtil.date());
            }
        });
        List<UserIntegralRecord> addIntegralList = integralRecordList.stream().filter(e -> ObjectUtil.isNull(e.getId())).collect(Collectors.toList());
        List<UserIntegralRecord> updateIntegralList = integralRecordList.stream()
                .filter(e -> ObjectUtil.isNotNull(e.getId()))
                .collect(Collectors.toList());

        StoreOrder tempOrder = new StoreOrder();
        tempOrder.setId(storeOrder.getId());
        tempOrder.setRefundStatus(2);
        // 佣金处理：未完成作废；已到账则扣回
        List<UserBrokerageRecord> brokerageRecordList = CollUtil.newArrayList();
        List<UserBrokerageRecord> clawbackBrokerageList = CollUtil.newArrayList();
        List<UserBrokerageRecord> recordList = userBrokerageRecordService.findListByLinkIdAndLinkType(storeOrder.getOrderId(), BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        if (CollUtil.isNotEmpty(recordList)) {
            recordList.forEach(r -> {
                if (r.getStatus() < BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE) {
                    r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION);
                    r.setUpdateTime(DateUtil.date());
                    brokerageRecordList.add(r);
                } else if (r.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE)
                        && BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD.equals(r.getType())) {
                    r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION);
                    r.setUpdateTime(DateUtil.date());
                    brokerageRecordList.add(r);
                    clawbackBrokerageList.add(r);
                }
            });
        }

        Boolean execute = transactionTemplate.execute(e -> {
            //写订单日志
            storeOrderStatusService.saveRefund(storeOrder.getId(), storeOrder.getRefundPrice(), "成功");

            // 更新用户数据
            user.setUpdateTime(DateUtil.date());
            userService.updateById(user);

            // 积分部分
            if (CollUtil.isNotEmpty(addIntegralList)) {
                userIntegralRecordService.saveBatch(addIntegralList);
            }
            if (CollUtil.isNotEmpty(updateIntegralList)) {
                userIntegralRecordService.updateBatchById(updateIntegralList);
            }
            // 已到账赠送积分扣回
            if (CollUtil.isNotEmpty(clawbackIntegralList)) {
                User integralUser = userService.getById(storeOrder.getUid());
                BigDecimal before = ObjectUtil.isNotNull(integralUser)
                        ? ObjectUtil.defaultIfNull(integralUser.getIntegral(), BigDecimal.ZERO)
                        : BigDecimal.ZERO;
                BigDecimal totalSub = clawbackIntegralList.stream()
                        .map(r -> ObjectUtil.defaultIfNull(r.getIntegral(), BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalSub.compareTo(BigDecimal.ZERO) > 0) {
                    Boolean integralOk = userService.operationIntegral(storeOrder.getUid(), totalSub, before, "sub");
                    if (!Boolean.TRUE.equals(integralOk)) {
                        throw new CrmebException("退款扣回积分失败");
                    }
                }
            }

            // 佣金处理
            if (CollUtil.isNotEmpty(brokerageRecordList)) {
                userBrokerageRecordService.updateBatchById(brokerageRecordList);
            }
            // 已到账佣金/团队奖扣回
            if (CollUtil.isNotEmpty(clawbackBrokerageList)) {
                Map<Integer, BigDecimal> brokerageCursor = new HashMap<>();
                for (UserBrokerageRecord r : clawbackBrokerageList) {
                    BigDecimal before = brokerageCursor.computeIfAbsent(r.getUid(), uid -> {
                        User bu = userService.getById(uid);
                        return ObjectUtil.isNotNull(bu)
                                ? ObjectUtil.defaultIfNull(bu.getBrokeragePrice(), BigDecimal.ZERO)
                                : BigDecimal.ZERO;
                    });
                    Boolean brokerageOk = userService.operationBrokerage(r.getUid(), r.getPrice(), before, "sub");
                    if (!Boolean.TRUE.equals(brokerageOk)) {
                        throw new CrmebException("退款扣回佣金失败，可能佣金已提现");
                    }
                    brokerageCursor.put(r.getUid(), before.subtract(r.getPrice()));
                }
            }

            // 经验/订单数处理
            if (ObjectUtil.isNotNull(refundExperienceRecord)) {
                userExperienceRecordService.save(refundExperienceRecord);
            }
            if (ObjectUtil.isNotNull(refundOrderCountRecord)) {
                userExperienceRecordService.save(refundOrderCountRecord);
            }
            if (levelStatsChanged) {
                userLevelService.downLevel(user);
            }

            // 团队等级：退款回滚自购/团队金额（等级只升不降，不自动清空）
            userTeamLevelService.rollbackTeamLevelOnRefund(storeOrder);

            // 回滚库存
            Boolean rollbackStock = rollbackStock(storeOrder);
            if (!rollbackStock) {
                throw new CrmebException("回滚库存失败");
            }

            tempOrder.setUpdateTime(DateUtil.date());
            storeOrderService.updateById(tempOrder);

            // 拼团状态处理
            if (storeOrder.getCombinationId() > 0) {
                StorePink storePink = storePinkService.getByOrderId(storeOrder.getOrderId());
                storePink.setStatus(3);
                storePink.setIsRefund(true);
                storePinkService.updateById(storePink);
            }

            // 退优惠券
            if (storeOrder.getCouponId() > 0 ) {
                StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                couponUser.setUpdateTime(DateUtil.date());
                couponUserService.updateById(couponUser);
            }
            return Boolean.TRUE;
        });
        return execute;
    }

    /**
     * 超时未支付系统自动取消
     */
    @Override
    public Boolean autoCancel(StoreOrder storeOrder) {
        // 判断订单是否支付
        if (storeOrder.getPaid()) {
            return Boolean.TRUE;
        }
        if (storeOrder.getIsDel() || storeOrder.getIsSystemDel()) {
            return Boolean.TRUE;
        }
        // 获取过期时间
        String cancelStr;
        DateTime cancelTime;
        if (storeOrder.getType().equals(1)) {
            cancelStr = "3";
            cancelTime = cn.hutool.core.date.DateUtil.offset(storeOrder.getCreateTime(), DateField.MINUTE, Integer.parseInt(cancelStr));
        } else {
            if (storeOrder.getBargainId() > 0 || storeOrder.getSeckillId() > 0 || storeOrder.getCombinationId() > 0) {
                cancelStr = systemConfigService.getValueByKey("order_activity_time");
            } else {
                cancelStr = systemConfigService.getValueByKey("order_cancel_time");
            }
            if (StrUtil.isBlank(cancelStr)) {
                cancelStr = "1";
            }
            cancelTime = cn.hutool.core.date.DateUtil.offset(storeOrder.getCreateTime(), DateField.HOUR_OF_DAY, Integer.parseInt(cancelStr));
        }
        long between = cn.hutool.core.date.DateUtil.between(cancelTime, cn.hutool.core.date.DateUtil.date(), DateUnit.SECOND, false);
        if (between < 0) {// 未到过期时间继续循环
            return Boolean.FALSE;
        }
        storeOrder.setIsDel(true).setIsSystemDel(true);
        Boolean execute = false;

        execute = transactionTemplate.execute(e -> {
            storeOrder.setUpdateTime(DateUtil.date());
            storeOrderService.updateById(storeOrder);
            //写订单日志
            storeOrderStatusService.createLog(storeOrder.getId(), "cancel", "到期未支付系统自动取消");
            // 退优惠券
            if (storeOrder.getCouponId() > 0 ) {
                StoreCouponUser couponUser = couponUserService.getById(storeOrder.getCouponId());
                couponUser.setStatus(CouponConstants.STORE_COUPON_USER_STATUS_USABLE);
                couponUser.setUpdateTime(DateUtil.date());
                couponUserService.updateById(couponUser);
            }
            // 回滚库存
            Boolean rollbackStock = rollbackStock(storeOrder);
            if (!rollbackStock) {
                throw new CrmebException("回滚库存失败");
            }
            return Boolean.TRUE;
        });

        return execute;
    }

    /**
     * 订单收货task处理
     * @param orderId 订单id
     * @return Boolean
     * 1.写订单日志
     * 2.分佣-佣金进入冻结期
     */
    @Override
    public Boolean orderReceiving(Integer orderId) {
        StoreOrder storeOrder = storeOrderService.getById(orderId);
        if (ObjectUtil.isNull(storeOrder)) {
            throw new CrmebException(StrUtil.format("订单收货task处理，未找到订单，id={}", orderId));
        }
        User user = userService.getById(storeOrder.getUid());

        // 获取佣金记录（历史待入账记录在收货时直接到账；已到账的跳过）
        List<UserBrokerageRecord> recordList = userBrokerageRecordService.findListByLinkIdAndLinkType(storeOrder.getOrderId(), BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        logger.info("收货处理佣金条数：" + recordList.size());
        List<UserBrokerageRecord> pendingBrokerageList = CollUtil.newArrayList();
        for (UserBrokerageRecord record : recordList) {
            if (record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE)
                    || record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_INVALIDATION)) {
                continue;
            }
            if (!record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE)
                    && !record.getStatus().equals(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_FROZEN)) {
                throw new CrmebException(StrUtil.format("订单收货task处理，订单佣金记录状态异常，id={}", orderId));
            }
            pendingBrokerageList.add(record);
        }

        // 获取积分记录（历史待入账赠送积分收货时直接到账）
        List<UserIntegralRecord> integralRecordList = userIntegralRecordService.findListByOrderIdAndUid(storeOrder.getOrderId(), storeOrder.getUid());
        logger.info("收货处理积分条数：" + integralRecordList.size());
        List<UserIntegralRecord> pendingIntegralList = integralRecordList.stream()
                .filter(e -> e.getType().equals(IntegralRecordConstants.INTEGRAL_RECORD_TYPE_ADD))
                .filter(e -> e.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_CREATE)
                        || e.getStatus().equals(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_FROZEN))
                .collect(Collectors.toList());

        Boolean execute = transactionTemplate.execute(e -> {
            // 日志
            storeOrderStatusService.createLog(storeOrder.getId(), "user_take_delivery", Constants.ORDER_STATUS_STR_TAKE);

            // 历史未到账佣金：收货直接入账
            if (CollUtil.isNotEmpty(pendingBrokerageList)) {
                Map<Integer, BigDecimal> brokerageCursor = new HashMap<>();
                for (UserBrokerageRecord record : pendingBrokerageList) {
                    BigDecimal before = brokerageCursor.computeIfAbsent(record.getUid(), uid -> {
                        User bu = userService.getById(uid);
                        return ObjectUtil.isNotNull(bu)
                                ? ObjectUtil.defaultIfNull(bu.getBrokeragePrice(), BigDecimal.ZERO)
                                : BigDecimal.ZERO;
                    });
                    BigDecimal after = before.add(ObjectUtil.defaultIfNull(record.getPrice(), BigDecimal.ZERO));
                    record.setBalance(after);
                    record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                    record.setFrozenTime(0);
                    record.setThawTime(cn.hutool.core.date.DateUtil.current(false));
                    record.setUpdateTime(DateUtil.date());
                    Boolean brokerageOk = userService.operationBrokerage(record.getUid(), record.getPrice(), before, "add");
                    if (!Boolean.TRUE.equals(brokerageOk)) {
                        throw new CrmebException("收货佣金到账失败");
                    }
                    brokerageCursor.put(record.getUid(), after);
                }
                userBrokerageRecordService.updateBatchById(pendingBrokerageList);
            }

            // 历史未到账赠送积分：收货直接入账
            if (CollUtil.isNotEmpty(pendingIntegralList)) {
                User integralUser = userService.getById(storeOrder.getUid());
                BigDecimal before = ObjectUtil.isNotNull(integralUser)
                        ? ObjectUtil.defaultIfNull(integralUser.getIntegral(), BigDecimal.ZERO)
                        : BigDecimal.ZERO;
                BigDecimal cursor = before;
                BigDecimal totalAdd = BigDecimal.ZERO;
                for (UserIntegralRecord record : pendingIntegralList) {
                    BigDecimal addAmount = ObjectUtil.defaultIfNull(record.getIntegral(), BigDecimal.ZERO);
                    totalAdd = totalAdd.add(addAmount);
                    cursor = cursor.add(addAmount);
                    record.setBalance(cursor);
                    record.setStatus(IntegralRecordConstants.INTEGRAL_RECORD_STATUS_COMPLETE);
                    record.setFrozenTime(0);
                    record.setThawTime(cn.hutool.core.date.DateUtil.current(false));
                    record.setUpdateTime(DateUtil.date());
                }
                if (totalAdd.compareTo(BigDecimal.ZERO) > 0) {
                    Boolean integralOk = userService.operationIntegral(storeOrder.getUid(), totalAdd, before, "add");
                    if (!Boolean.TRUE.equals(integralOk)) {
                        throw new CrmebException("收货积分到账失败");
                    }
                }
                userIntegralRecordService.updateBatchById(pendingIntegralList);
            }
            return Boolean.TRUE;
        });
        if (execute) {
            // 发送用户确认收货管理员提醒短信
            SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.RECEIPT_GOODS_ADMIN_MARK);
            if (notification.getIsSms().equals(1)) {
                // 查询可已发送短信的管理员
                List<SystemAdmin> systemAdminList = systemAdminService.findIsSmsList();
                if (CollUtil.isNotEmpty(systemAdminList)) {
                    SmsTemplate smsTemplate = smsTemplateService.getDetail(notification.getSmsId());
                    Integer tempId = Integer.valueOf(smsTemplate.getTempId());
                    // 发送短信
                    systemAdminList.forEach(admin -> {
                        smsService.sendOrderReceiptNotice(admin.getPhone(), storeOrder.getOrderId(), admin.getRealName(), tempId);
                    });
                }
            }

            // 发送消息通知
            pushMessageOrder(storeOrder, user);
        }

        return execute;
    }

    /**
     * 发送消息通知
     * 根据用户类型发送
     * 公众号模板消息
     * 小程序订阅消息
     */
    private void pushMessageOrder(StoreOrder storeOrder, User user) {
        SystemNotification notification = systemNotificationService.getByMark(NotifyConstants.RECEIPT_GOODS_MARK);
        if (storeOrder.getIsChannel().equals(2)) {
            return;
        }
        if (!storeOrder.getPayType().equals(Constants.PAY_TYPE_WE_CHAT)) {
            return;
        }
        UserToken userToken;
        HashMap<String, String> temMap = new HashMap<>();
        // 公众号
        if (storeOrder.getIsChannel().equals(Constants.ORDER_PAY_CHANNEL_PUBLIC) && notification.getIsWechat().equals(1)) {
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_WECHAT);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 发送微信模板消息
            temMap.put(Constants.WE_CHAT_TEMP_KEY_FIRST, "您购买的商品已确认收货！");
            temMap.put("keyword1", storeOrder.getOrderId());
            temMap.put("keyword2", "已收货");
            temMap.put("keyword3", CrmebDateUtil.nowDateTimeStr());
            temMap.put("keyword4", "详情请进入订单查看");
            temMap.put(Constants.WE_CHAT_TEMP_KEY_END, "感谢你的使用。");
            templateMessageService.pushTemplateMessage(notification.getWechatId(), temMap, userToken.getToken());
        } else if (notification.getIsRoutine().equals(1)) {
            // 小程序发送订阅消息
            userToken = userTokenService.getTokenByUserId(user.getUid(), UserConstants.USER_TOKEN_TYPE_ROUTINE);
            if (ObjectUtil.isNull(userToken)) {
                return ;
            }
            // 组装数据
            // 获取商品名称
            String storeNameAndCarNumString = orderUtils.getStoreNameAndCarNumString(storeOrder.getId());
            if (StrUtil.isBlank(storeNameAndCarNumString)) {
                return ;
            }
            if (storeNameAndCarNumString.length() > 20) {
                storeNameAndCarNumString = storeNameAndCarNumString.substring(0, 15) + "***";
            }
//        temMap.put("character_string6", storeOrder.getOrderId());
//        temMap.put("phrase4", "已收货");
//        temMap.put("time7", DateUtil.nowDateTimeStr());
//        temMap.put("thing1", storeNameAndCarNumString);
//        temMap.put("thing5", "您购买的商品已确认收货！");
            temMap.put("character_string6", storeOrder.getOrderId());
            temMap.put("date5", CrmebDateUtil.nowDateTimeStr());
            temMap.put("thing2", storeNameAndCarNumString);
            templateMessageService.pushMiniTemplateMessage(notification.getRoutineId(), temMap, userToken.getToken());
        }
    }
}
