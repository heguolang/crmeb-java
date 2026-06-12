package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.UserLevelConstants;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.constants.ExperienceRecordConstants;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.system.SystemUserLevel;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserExperienceRecord;
import com.zbkj.common.model.user.UserLevel;
import com.zbkj.service.dao.UserLevelDao;
import com.zbkj.service.service.SystemUserLevelService;
import com.zbkj.service.service.UserExperienceRecordService;
import com.zbkj.service.service.UserLevelService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserLevelServiceImpl 接口实现
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
public class UserLevelServiceImpl extends ServiceImpl<UserLevelDao, UserLevel> implements UserLevelService {

    @Resource
    private UserLevelDao dao;

    @Autowired
    private SystemUserLevelService systemUserLevelService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserExperienceRecordService userExperienceRecordService;

    @Autowired
    private TransactionTemplate transactionTemplate;


    /**
    * 列表
    * @param pageParamRequest 分页类参数
    * @return List<UserLevel>
    */
    @Override
    public List<UserLevel> getList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        return dao.selectList(null);
    }

    /**
     * 用户升级
     * @param user 用户数据
     * @return Boolean
     */
    @Override
    public Boolean upLevel(User user) {
        List<SystemUserLevel> list = systemUserLevelService.getUsableList();
        if (CollUtil.isEmpty(list)) {
            log.error("系统会员等级未配置，请配置对应数据");
            return Boolean.TRUE;
        }

        SystemUserLevel userLevelConfig = resolveMatchedLevel(user, list);

        if(ObjectUtil.isNull(userLevelConfig)) {
            log.warn("未找到用户对应的会员等级,uid = " + user.getUid());
            return Boolean.TRUE;
        }

        // 判断用户是否还在原等级
        if (ObjectUtil.defaultIfNull(user.getLevel(), 0).equals(userLevelConfig.getId())) {
            return Boolean.TRUE;
        }

        // 判断用户等级经过向上调整
        List<SystemUserLevel> collect = list.stream()
                .filter(e -> e.getId().equals(ObjectUtil.defaultIfNull(user.getLevel(), 0)))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(collect)) {
            if (collect.get(0).getGrade() > userLevelConfig.getGrade()) {
                return Boolean.TRUE;
            }
        }
        return saveUserLevelChange(user, userLevelConfig, Constants.USER_LEVEL_UP_LOG_MARK);
    }

    /**
     * 经验降级
     * @param user 用户
     * @return Boolean
     */
    @Override
    public Boolean downLevel(User user) {
        List<SystemUserLevel> list = systemUserLevelService.getUsableList();
        if (CollUtil.isEmpty(list)) {
            log.error("系统会员等级未配置，请配置对应数据");
            return Boolean.TRUE;
        }

        SystemUserLevel userLevelConfig = resolveMatchedLevel(user, list);

        if(ObjectUtil.isNull(userLevelConfig)) {
            log.warn("未找到用户对应的会员等级,uid = " + user.getUid());
            return Boolean.TRUE;
        }

        // 判断用户是否还在原等级
        if (ObjectUtil.defaultIfNull(user.getLevel(), 0).equals(userLevelConfig.getId())) {
            return Boolean.TRUE;
        }

        return saveUserLevelChange(user, userLevelConfig, Constants.USER_LEVEL_OPERATE_LOG_MARK);
    }

    /**
     * 删除（通过系统等级id）
     * @param levelId 系统等级id
     * @return Boolean
     */
    @Override
    public Boolean deleteByLevelId(Integer levelId) {
        LambdaUpdateWrapper<UserLevel> luw = Wrappers.lambdaUpdate();
        luw.set(UserLevel::getIsDel, true);
        luw.eq(UserLevel::getLevelId, levelId);
        luw.eq(UserLevel::getIsDel, false);
        return update(luw);
    }

    @Override
    public SystemUserLevel resolveMatchedLevel(User user) {
        List<SystemUserLevel> list = systemUserLevelService.getUsableList();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return resolveMatchedLevel(user, list);
    }

    @Override
    public Integer getGiveIntegral(User user) {
        SystemUserLevel matchedLevel = resolveMatchedLevel(user);
        if (ObjectUtil.isNull(matchedLevel)) {
            return UserLevelConstants.DEFAULT_GIVE_INTEGRAL;
        }
        return ObjectUtil.defaultIfNull(matchedLevel.getGiveIntegral(), UserLevelConstants.DEFAULT_GIVE_INTEGRAL);
    }

    @Override
    public Boolean processLevelOnOrderComplete(StoreOrder storeOrder) {
        boolean needConsumption = Boolean.TRUE.equals(systemUserLevelService.hasConsumptionTriggerOnComplete());
        boolean needOrderCount = Boolean.TRUE.equals(systemUserLevelService.hasOrderCountTriggerOnComplete());
        if (!needConsumption && !needOrderCount) {
            return Boolean.TRUE;
        }

        User user = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(user)) {
            return Boolean.FALSE;
        }

        UserExperienceRecord consumptionRecord = null;
        UserExperienceRecord orderCountRecord = null;
        boolean changed = false;

        if (needConsumption) {
            UserExperienceRecord existRecord = userExperienceRecordService.getByOrderNoAndUid(
                    storeOrder.getOrderId(), storeOrder.getUid());
            if (ObjectUtil.isNull(existRecord)) {
                int experience = storeOrder.getPayPrice().setScale(0, BigDecimal.ROUND_DOWN).intValue();
                if (experience > 0) {
                    user.setExperience(ObjectUtil.defaultIfNull(user.getExperience(), 0) + experience);
                    consumptionRecord = buildExperienceRecord(storeOrder, user.getExperience(), experience,
                            ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER,
                            ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_ORDER_COMPLETE,
                            "订单交易完成增加" + experience + "经验");
                    changed = true;
                }
            }
        }

        if (needOrderCount) {
            UserExperienceRecord existOrderCountRecord = userExperienceRecordService.getByOrderNoAndUidAndLinkType(
                    storeOrder.getOrderId(), storeOrder.getUid(),
                    ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER_COUNT);
            if (ObjectUtil.isNull(existOrderCountRecord)) {
                int completeOrderCount = userExperienceRecordService.countCompleteOrderByUid(storeOrder.getUid()) + 1;
                orderCountRecord = buildExperienceRecord(storeOrder, completeOrderCount, 1,
                        ExperienceRecordConstants.EXPERIENCE_RECORD_LINK_TYPE_ORDER_COUNT,
                        ExperienceRecordConstants.EXPERIENCE_RECORD_TITLE_ORDER_COUNT_COMPLETE,
                        "订单交易完成累计1笔订单");
                changed = true;
            }
        }

        if (!changed) {
            return Boolean.TRUE;
        }

        final UserExperienceRecord finalConsumptionRecord = consumptionRecord;
        final UserExperienceRecord finalOrderCountRecord = orderCountRecord;
        return transactionTemplate.execute(e -> {
            if (ObjectUtil.isNotNull(finalConsumptionRecord)) {
                userExperienceRecordService.save(finalConsumptionRecord);
            }
            if (ObjectUtil.isNotNull(finalOrderCountRecord)) {
                userExperienceRecordService.save(finalOrderCountRecord);
            }
            user.setUpdateTime(DateUtil.date());
            userService.updateById(user);
            upLevel(user);
            return Boolean.TRUE;
        });
    }

    private UserExperienceRecord buildExperienceRecord(StoreOrder storeOrder, Integer balance, Integer value,
                                                       String linkType, String title, String mark) {
        UserExperienceRecord record = new UserExperienceRecord();
        record.setUid(storeOrder.getUid());
        record.setLinkId(storeOrder.getOrderId());
        record.setLinkType(linkType);
        record.setType(ExperienceRecordConstants.EXPERIENCE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setExperience(value);
        record.setBalance(balance);
        record.setMark(mark);
        record.setStatus(ExperienceRecordConstants.EXPERIENCE_RECORD_STATUS_CREATE);
        record.setCreateTime(DateUtil.date());
        return record;
    }

    @Override
    public Integer getProjectedGiveIntegral(User user, BigDecimal payAmount) {
        User projectedUser = buildProjectedUserAfterPay(user, payAmount);
        int additionalConsumption = getAdditionalPaidConsumption(payAmount);
        SystemUserLevel matchedLevel = resolveMatchedLevel(projectedUser, additionalConsumption);
        if (ObjectUtil.isNull(matchedLevel)) {
            return UserLevelConstants.DEFAULT_GIVE_INTEGRAL;
        }
        return ObjectUtil.defaultIfNull(matchedLevel.getGiveIntegral(), UserLevelConstants.DEFAULT_GIVE_INTEGRAL);
    }

    /**
     * 模拟本单支付后用户升级统计数据（用于积分/等级预览）
     */
    private User buildProjectedUserAfterPay(User user, BigDecimal payAmount) {
        User projectedUser = new User();
        projectedUser.setUid(user.getUid());
        projectedUser.setExperience(ObjectUtil.defaultIfNull(user.getExperience(), 0));
        projectedUser.setPayCount(ObjectUtil.defaultIfNull(user.getPayCount(), 0));
        int additionalConsumption = getAdditionalPaidConsumption(payAmount);
        if (additionalConsumption > 0) {
            projectedUser.setExperience(projectedUser.getExperience() + additionalConsumption);
        }
        if (Boolean.TRUE.equals(systemUserLevelService.hasOrderCountTriggerOnPaid())) {
            projectedUser.setPayCount(projectedUser.getPayCount() + 1);
        }
        return projectedUser;
    }

    private int getAdditionalPaidConsumption(BigDecimal payAmount) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED
                || !Boolean.TRUE.equals(systemUserLevelService.hasConsumptionTriggerOnPaid())) {
            return 0;
        }
        return payAmount.setScale(0, BigDecimal.ROUND_DOWN).intValue();
    }

    /**
     * 根据升级条件匹配用户当前应达到的等级
     */
    private SystemUserLevel resolveMatchedLevel(User user, List<SystemUserLevel> list) {
        return resolveMatchedLevel(user, list, 0);
    }

    private SystemUserLevel resolveMatchedLevel(User user, int additionalConsumption) {
        List<SystemUserLevel> list = systemUserLevelService.getUsableList();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return resolveMatchedLevel(user, list, additionalConsumption);
    }

    private SystemUserLevel resolveMatchedLevel(User user, List<SystemUserLevel> list, int additionalConsumption) {
        return list.stream()
                .filter(level -> meetsUpgradeCondition(user, level, additionalConsumption))
                .max(Comparator.comparing(SystemUserLevel::getGrade))
                .orElse(null);
    }

    /**
     * 根据等级配置判断是否满足升级条件
     */
    private boolean meetsUpgradeCondition(User user, SystemUserLevel level) {
        return meetsUpgradeCondition(user, level, 0);
    }

    private boolean meetsUpgradeCondition(User user, SystemUserLevel level, int additionalConsumption) {
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            upgradeType = UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT;
        }

        if (UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(upgradeType)) {
            int orderCountThreshold = ObjectUtil.defaultIfNull(level.getUpgradeValue(), 0);
            return getOrderCountForLevel(user, level) >= orderCountThreshold;
        }
        if (UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType)) {
            int consumptionThreshold = ObjectUtil.defaultIfNull(level.getExperience(), 0);
            int orderCountThreshold = ObjectUtil.defaultIfNull(level.getUpgradeValue(), 0);
            return getConsumptionForLevel(user, level, additionalConsumption) >= consumptionThreshold
                    && getOrderCountForLevel(user, level) >= orderCountThreshold;
        }
        int consumptionThreshold = ObjectUtil.defaultIfNull(level.getExperience(), 0);
        return getConsumptionForLevel(user, level, additionalConsumption) >= consumptionThreshold;
    }

    /**
     * 按等级配置的订单数统计时机获取有效订单数
     */
    private int getOrderCountForLevel(User user, SystemUserLevel level) {
        if (!levelRequiresOrderCount(level)) {
            return 0;
        }
        Integer triggerType = ObjectUtil.defaultIfNull(level.getOrderCountTriggerType(),
                UserLevelConstants.ORDER_COUNT_TRIGGER_PAID);
        if (UserLevelConstants.ORDER_COUNT_TRIGGER_COMPLETE.equals(triggerType)) {
            return ObjectUtil.defaultIfNull(userExperienceRecordService.countCompleteOrderByUid(user.getUid()), 0);
        }
        return ObjectUtil.defaultIfNull(user.getPayCount(), 0);
    }

    private boolean levelRequiresOrderCount(SystemUserLevel level) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            return true;
        }
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        return UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(upgradeType)
                || UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType);
    }

    /**
     * 按等级配置的消费金额统计时机获取有效消费经验（1元=1经验）
     */
    private int getConsumptionForLevel(User user, SystemUserLevel level) {
        return getConsumptionForLevel(user, level, 0);
    }

    private int getConsumptionForLevel(User user, SystemUserLevel level, int additionalConsumption) {
        if (!levelRequiresConsumption(level)) {
            return 0;
        }
        Integer triggerType = ObjectUtil.defaultIfNull(level.getConsumptionTriggerType(),
                UserLevelConstants.CONSUMPTION_TRIGGER_PAID);
        if (UserLevelConstants.CONSUMPTION_TRIGGER_COMPLETE.equals(triggerType)) {
            return ObjectUtil.defaultIfNull(userExperienceRecordService.sumCompleteConsumptionByUid(user.getUid()), 0);
        }
        int paidConsumption = ObjectUtil.defaultIfNull(
                userExperienceRecordService.sumPaidConsumptionByUid(user.getUid()), 0) + additionalConsumption;
        int userExperience = ObjectUtil.defaultIfNull(user.getExperience(), 0);
        if (paidConsumption <= 0) {
            return userExperience;
        }
        return Math.max(paidConsumption, userExperience);
    }

    private boolean levelRequiresConsumption(SystemUserLevel level) {
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        return UserLevelConstants.UPGRADE_TYPE_CONSUMPTION.equals(upgradeType)
                || UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType);
    }

    /**
     * 保存用户等级变更记录
     */
    private Boolean saveUserLevelChange(User user, SystemUserLevel userLevelConfig, String markTemplate) {
        UserLevel newLevel = new UserLevel();
        newLevel.setStatus(true);
        newLevel.setIsDel(false);
        newLevel.setGrade(userLevelConfig.getGrade());
        newLevel.setUid(user.getUid());
        newLevel.setLevelId(userLevelConfig.getId());
        newLevel.setDiscount(userLevelConfig.getDiscount());
        newLevel.setGiveIntegral(ObjectUtil.defaultIfNull(
                userLevelConfig.getGiveIntegral(), UserLevelConstants.DEFAULT_GIVE_INTEGRAL));

        Date date = CrmebDateUtil.nowDateTimeReturnDate(Constants.DATE_FORMAT);
        String mark = markTemplate.replace("【{$userName}】", user.getNickname()).
                replace("{$date}", CrmebDateUtil.dateToStr(date, Constants.DATE_FORMAT)).
                replace("{$levelName}", userLevelConfig.getName());
        newLevel.setMark(mark);

        user.setLevel(userLevelConfig.getId());
        return transactionTemplate.execute(e -> {
            save(newLevel);
            user.setUpdateTime(DateUtil.date());
            userService.updateById(user);
            return Boolean.TRUE;
        });
    }
}
