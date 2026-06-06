package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.constants.UserLevelConstants;
import com.zbkj.common.exception.CrmebException;
import cn.hutool.core.collection.CollUtil;
import com.zbkj.common.model.system.SystemUserLevel;
import com.zbkj.common.model.system.SystemUserLevelBrokerage;
import com.zbkj.common.request.SystemUserLevelRequest;
import com.zbkj.common.request.SystemUserLevelUpdateShowRequest;
import com.zbkj.common.response.SystemUserLevelInfoResponse;
import com.zbkj.service.dao.SystemUserLevelDao;
import com.zbkj.service.service.SystemAttachmentService;
import com.zbkj.service.service.SystemUserLevelBrokerageService;
import com.zbkj.service.service.SystemUserLevelService;
import com.zbkj.service.service.UserLevelService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SystemUserLevelServiceImpl 接口实现
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
public class SystemUserLevelServiceImpl extends ServiceImpl<SystemUserLevelDao, SystemUserLevel> implements SystemUserLevelService {

    @Resource
    private SystemUserLevelDao dao;

    @Autowired
    private SystemAttachmentService systemAttachmentService;
    @Autowired
    private UserLevelService userLevelService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SystemUserLevelBrokerageService systemUserLevelBrokerageService;


    /**
     * 分页显示设置用户等级表
     * @return List<SystemUserLevel>
     */
    @Override
    public List<SystemUserLevel> getList() {
        return getAllList();
    }

    @Override
    public List<SystemUserLevel> getAllList() {
        LambdaQueryWrapper<SystemUserLevel> levelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        levelLambdaQueryWrapper.eq(SystemUserLevel::getIsDel, false);
        levelLambdaQueryWrapper.orderByAsc(SystemUserLevel::getGrade);
        List<SystemUserLevel> levelList = dao.selectList(levelLambdaQueryWrapper);
        fillBrokerageConfig(levelList);
        return levelList;
    }

    @Override
    public SystemUserLevelInfoResponse getInfo(Integer id) {
        SystemUserLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("等级不存在");
        }
        SystemUserLevelInfoResponse response = new SystemUserLevelInfoResponse();
        response.setLevel(level);
        response.setBrokerage(systemUserLevelBrokerageService.getByLevelId(id));
        return response;
    }

    /**
     * 新增设置用户等级表
     * @param request SystemUserLevelRequest 新增参数
     * @return boolean
     * 等级名称不能重复
     * 等级级别不能重复
     */
    @Override
    public Boolean create(SystemUserLevelRequest request) {
        normalizeLevelRequest(request);
        checkLevel(request);
        SystemUserLevel systemUserLevel = buildLevelEntity(request);
        systemUserLevel.setCreateTime(DateUtil.date());
        systemUserLevel.setUpdateTime(DateUtil.date());
        return transactionTemplate.execute(e -> {
            if (!save(systemUserLevel)) {
                return Boolean.FALSE;
            }
            systemUserLevelBrokerageService.saveOrUpdateByLevelId(systemUserLevel.getId(), request.getBrokerage());
            return Boolean.TRUE;
        });
    }

    /**
     * 添加、修改校验
     * @param request 用户等级参数
     * 等级名称不能重复
     * 等级级别不能重复
     */
    private void normalizeLevelRequest(SystemUserLevelRequest request) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            request.setUpgradeType(UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT);
            request.setExperience(0);
            if (ObjectUtil.isNull(request.getOrderCountTriggerType())) {
                request.setOrderCountTriggerType(UserLevelConstants.ORDER_COUNT_TRIGGER_PAID);
            }
            if (ObjectUtil.isNull(request.getUpgradeValue())) {
                throw new CrmebException("累计订单数升级门槛不能为空");
            }
            if (!UserLevelConstants.ORDER_COUNT_TRIGGER_PAID.equals(request.getOrderCountTriggerType())
                    && !UserLevelConstants.ORDER_COUNT_TRIGGER_COMPLETE.equals(request.getOrderCountTriggerType())) {
                throw new CrmebException("订单数统计时机不正确，仅支持1=已付款，2=交易完成");
            }
            return;
        }
        if (ObjectUtil.isNull(request.getUpgradeType())) {
            request.setUpgradeType(UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        }
        if (ObjectUtil.isNull(request.getGiveIntegral())) {
            request.setGiveIntegral(UserLevelConstants.DEFAULT_GIVE_INTEGRAL);
        }
        if (ObjectUtil.isNull(request.getConsumptionTriggerType())) {
            request.setConsumptionTriggerType(UserLevelConstants.CONSUMPTION_TRIGGER_PAID);
        }
        if (ObjectUtil.isNull(request.getOrderCountTriggerType())) {
            request.setOrderCountTriggerType(UserLevelConstants.ORDER_COUNT_TRIGGER_PAID);
        }
        if (!UserLevelConstants.CONSUMPTION_TRIGGER_PAID.equals(request.getConsumptionTriggerType())
                && !UserLevelConstants.CONSUMPTION_TRIGGER_COMPLETE.equals(request.getConsumptionTriggerType())) {
            throw new CrmebException("消费金额统计时机不正确，仅支持1=已付款，2=交易完成");
        }
        if (!UserLevelConstants.ORDER_COUNT_TRIGGER_PAID.equals(request.getOrderCountTriggerType())
                && !UserLevelConstants.ORDER_COUNT_TRIGGER_COMPLETE.equals(request.getOrderCountTriggerType())) {
            throw new CrmebException("订单数统计时机不正确，仅支持1=已付款，2=交易完成");
        }
        if (!UserLevelConstants.UPGRADE_TYPE_CONSUMPTION.equals(request.getUpgradeType())
                && !UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(request.getUpgradeType())
                && !UserLevelConstants.UPGRADE_TYPE_BOTH.equals(request.getUpgradeType())) {
            throw new CrmebException("升级条件类型不正确，仅支持1=累计消费金额，2=累计订单数，3=两者同时满足");
        }
        if (UserLevelConstants.UPGRADE_TYPE_CONSUMPTION.equals(request.getUpgradeType())) {
            if (ObjectUtil.isNull(request.getExperience())) {
                throw new CrmebException("累计消费金额升级门槛不能为空");
            }
            request.setUpgradeValue(0);
            return;
        }
        if (UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(request.getUpgradeType())) {
            if (ObjectUtil.isNull(request.getUpgradeValue())) {
                throw new CrmebException("累计订单数升级门槛不能为空");
            }
            request.setExperience(0);
            return;
        }
        if (ObjectUtil.isNull(request.getExperience())) {
            throw new CrmebException("累计消费金额升级门槛不能为空");
        }
        if (ObjectUtil.isNull(request.getUpgradeValue())) {
            throw new CrmebException("累计订单数升级门槛不能为空");
        }
    }

    private SystemUserLevel buildLevelEntity(SystemUserLevelRequest request) {
        SystemUserLevel systemUserLevel = new SystemUserLevel();
        BeanUtils.copyProperties(request, systemUserLevel);
        systemUserLevel.setIcon(systemAttachmentService.clearPrefix(request.getIcon()));
        fillDefaultLevelConfig(systemUserLevel);
        return systemUserLevel;
    }

    private void checkLevel(SystemUserLevelRequest request) {
        SystemUserLevel temp;
        // 校验名称
        LambdaQueryWrapper<SystemUserLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemUserLevel::getName, request.getName());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemUserLevel::getId, request.getId());
        }
        lqw.eq(SystemUserLevel::getIsDel, false);
        temp = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(temp)) {
            throw new CrmebException("用户等级名称重复");
        }
        // 校验等级级别
        lqw.clear();
        lqw.eq(SystemUserLevel::getGrade, request.getGrade());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemUserLevel::getId, request.getId());
        }
        lqw.eq(SystemUserLevel::getIsDel, false);
        temp = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(temp)) {
            throw new CrmebException("用户等级级别重复");
        }
        // 校验等级门槛不能比上一级别的低，不能比下一级别的高
        if (request.getGrade() > 1) {
            lqw.clear();
            lqw.lt(SystemUserLevel::getGrade, request.getGrade());
            if (ObjectUtil.isNotNull(request.getId())) {
                lqw.ne(SystemUserLevel::getId, request.getId());
            }
            lqw.eq(SystemUserLevel::getIsDel, false);
            lqw.orderByDesc(SystemUserLevel::getGrade);
            lqw.last(" limit 1");
            temp = dao.selectOne(lqw);
            if (ObjectUtil.isNotNull(temp) && getThresholdValue(temp) >= getThresholdValue(request)) {
                throw new CrmebException("当前等级的升级门槛不能比上一级别的低");
            }
        }
        lqw.clear();
        lqw.gt(SystemUserLevel::getGrade, request.getGrade());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemUserLevel::getId, request.getId());
        }
        lqw.eq(SystemUserLevel::getIsDel, false);
        lqw.orderByAsc(SystemUserLevel::getGrade);
        lqw.last(" limit 1");
        temp = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(temp) && getThresholdValue(temp) <= getThresholdValue(request)) {
            throw new CrmebException("当前等级的升级门槛不能比下一级别的高");
        }
    }

    private Integer getThresholdValue(SystemUserLevel level) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            return ObjectUtil.defaultIfNull(level.getUpgradeValue(), 0);
        }
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        if (UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(upgradeType)) {
            return ObjectUtil.defaultIfNull(level.getUpgradeValue(), 0);
        }
        if (UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType)) {
            return ObjectUtil.defaultIfNull(level.getExperience(), 0) + ObjectUtil.defaultIfNull(level.getUpgradeValue(), 0);
        }
        return ObjectUtil.defaultIfNull(level.getExperience(), 0);
    }

    private Integer getThresholdValue(SystemUserLevelRequest request) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            return ObjectUtil.defaultIfNull(request.getUpgradeValue(), 0);
        }
        if (UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(request.getUpgradeType())) {
            return ObjectUtil.defaultIfNull(request.getUpgradeValue(), 0);
        }
        if (UserLevelConstants.UPGRADE_TYPE_BOTH.equals(request.getUpgradeType())) {
            return ObjectUtil.defaultIfNull(request.getExperience(), 0) + ObjectUtil.defaultIfNull(request.getUpgradeValue(), 0);
        }
        return ObjectUtil.defaultIfNull(request.getExperience(), 0);
    }

    private void fillDefaultLevelConfig(SystemUserLevel systemUserLevel) {
        if (ObjectUtil.isNull(systemUserLevel.getUpgradeType())) {
            systemUserLevel.setUpgradeType(UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        }
        if (ObjectUtil.isNull(systemUserLevel.getGiveIntegral())) {
            systemUserLevel.setGiveIntegral(UserLevelConstants.DEFAULT_GIVE_INTEGRAL);
        }
        if (ObjectUtil.isNull(systemUserLevel.getUpgradeValue())) {
            systemUserLevel.setUpgradeValue(0);
        }
        if (ObjectUtil.isNull(systemUserLevel.getConsumptionTriggerType())) {
            systemUserLevel.setConsumptionTriggerType(UserLevelConstants.CONSUMPTION_TRIGGER_PAID);
        }
        if (ObjectUtil.isNull(systemUserLevel.getOrderCountTriggerType())) {
            systemUserLevel.setOrderCountTriggerType(UserLevelConstants.ORDER_COUNT_TRIGGER_PAID);
        }
    }


    /**
     * 系统等级更新
     * @param id    等级id
     * @param request   等级数据
     * @return Boolean
     */
    @Override
    public Boolean update(Integer id, SystemUserLevelRequest request) {
        SystemUserLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("等级不存在");
        }
        request.setId(id);
        normalizeLevelRequest(request);
        checkLevel(request);
        SystemUserLevel systemUserLevel = buildLevelEntity(request);
        systemUserLevel.setId(id);
        systemUserLevel.setIsShow(level.getIsShow());
        return transactionTemplate.execute(e -> {
            systemUserLevel.setUpdateTime(DateUtil.date());
            dao.updateById(systemUserLevel);
            systemUserLevelBrokerageService.saveOrUpdateByLevelId(id, request.getBrokerage());
            // 删除对应的用户等级数据
            userLevelService.deleteByLevelId(id);
            // 清除对应的用户等级
            userService.removeLevelByLevelId(id);
            return Boolean.TRUE;
        });
    }

    @Override
    public SystemUserLevel getByLevelId(Integer levelId) {
        LambdaQueryWrapper<SystemUserLevel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SystemUserLevel::getIsShow, 1);
        lambdaQueryWrapper.eq(SystemUserLevel::getIsDel, 0);
        lambdaQueryWrapper.eq(SystemUserLevel::getId, levelId);
        return dao.selectOne(lambdaQueryWrapper);
    }

    /**
     * 获取系统等级列表（移动端）
     */
    @Override
    public List<SystemUserLevel> getH5LevelList() {
        LambdaQueryWrapper<SystemUserLevel> lqw = new LambdaQueryWrapper<>();
        lqw.select(SystemUserLevel::getId, SystemUserLevel::getName, SystemUserLevel::getIcon,
                SystemUserLevel::getExperience, SystemUserLevel::getGrade, SystemUserLevel::getDiscount,
                SystemUserLevel::getUpgradeType, SystemUserLevel::getConsumptionTriggerType,
                SystemUserLevel::getOrderCountTriggerType, SystemUserLevel::getUpgradeValue,
                SystemUserLevel::getGiveIntegral, SystemUserLevel::getDescription);
        lqw.eq(SystemUserLevel::getIsShow, true);
        lqw.eq(SystemUserLevel::getIsDel, false);
        lqw.orderByAsc(SystemUserLevel::getGrade);
        return dao.selectList(lqw);
    }

    /**
     * 删除系统等级
     * @param id 等级id
     * @return Boolean
     */
    @Override
    public Boolean delete(Integer id) {
        SystemUserLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("系统等级不存在");
        }
        level.setIsDel(true);
        return transactionTemplate.execute(e -> {
            level.setUpdateTime(DateUtil.date());
            dao.updateById(level);
            systemUserLevelBrokerageService.deleteByLevelId(id);
            // 删除对应的用户等级数据
            userLevelService.deleteByLevelId(id);
            // 清除对应的用户等级
            userService.removeLevelByLevelId(id);
            return Boolean.TRUE;
        });
    }

    private void fillBrokerageConfig(List<SystemUserLevel> levelList) {
        if (CollUtil.isEmpty(levelList)) {
            return;
        }
        List<Integer> levelIds = levelList.stream().map(SystemUserLevel::getId).collect(Collectors.toList());
        Map<Integer, SystemUserLevelBrokerage> brokerageMap = systemUserLevelBrokerageService.mapByLevelIds(levelIds);
        levelList.forEach(level -> level.setBrokerage(brokerageMap.get(level.getId())));
    }

    /**
     * 使用/禁用
     * @param request request
     */
    @Override
    public Boolean updateShow(SystemUserLevelUpdateShowRequest request) {
        SystemUserLevel level = getById(request.getId());
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("等级不存在");
        }
        if (level.getIsShow().equals(request.getIsShow())) {
            return Boolean.TRUE;
        }
        level.setIsShow(request.getIsShow());
        if (request.getIsShow()) {// 启用直接保存
            level.setUpdateTime(DateUtil.date());
            return dao.updateById(level) > 0 ? Boolean.TRUE : Boolean.FALSE;
        }
        return transactionTemplate.execute(e -> {
            level.setUpdateTime(DateUtil.date());
            dao.updateById(level);
            // 删除对应的用户等级数据
            userLevelService.deleteByLevelId(request.getId());
            // 清除对应的用户等级
            userService.removeLevelByLevelId(request.getId());
            return Boolean.TRUE;
        });
    }

    /**
     * 获取可用等级列表
     * @return List
     */
    @Override
    public List<SystemUserLevel> getUsableList() {
        LambdaQueryWrapper<SystemUserLevel> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SystemUserLevel::getIsShow, true);
        lqw.eq(SystemUserLevel::getIsDel, false);
        lqw.orderByAsc(SystemUserLevel::getGrade);
        return dao.selectList(lqw);
    }

    @Override
    public Boolean hasConsumptionTriggerOnPaid() {
        return getUsableList().stream().anyMatch(this::levelRequiresConsumptionOnPaid);
    }

    @Override
    public Boolean hasConsumptionTriggerOnComplete() {
        return getUsableList().stream().anyMatch(this::levelRequiresConsumptionOnComplete);
    }

    @Override
    public Boolean hasOrderCountTriggerOnPaid() {
        return getUsableList().stream().anyMatch(this::levelRequiresOrderCountOnPaid);
    }

    @Override
    public Boolean hasOrderCountTriggerOnComplete() {
        return getUsableList().stream().anyMatch(this::levelRequiresOrderCountOnComplete);
    }

    private boolean levelRequiresConsumption(SystemUserLevel level) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            return false;
        }
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        return UserLevelConstants.UPGRADE_TYPE_CONSUMPTION.equals(upgradeType)
                || UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType);
    }

    private boolean levelRequiresOrderCount(SystemUserLevel level) {
        if (!UserLevelConstants.EXPERIENCE_UPGRADE_ENABLED) {
            return true;
        }
        Integer upgradeType = ObjectUtil.defaultIfNull(level.getUpgradeType(), UserLevelConstants.UPGRADE_TYPE_CONSUMPTION);
        return UserLevelConstants.UPGRADE_TYPE_ORDER_COUNT.equals(upgradeType)
                || UserLevelConstants.UPGRADE_TYPE_BOTH.equals(upgradeType);
    }

    private boolean levelRequiresConsumptionOnPaid(SystemUserLevel level) {
        return levelRequiresConsumption(level)
                && UserLevelConstants.CONSUMPTION_TRIGGER_PAID.equals(
                ObjectUtil.defaultIfNull(level.getConsumptionTriggerType(), UserLevelConstants.CONSUMPTION_TRIGGER_PAID));
    }

    private boolean levelRequiresConsumptionOnComplete(SystemUserLevel level) {
        return levelRequiresConsumption(level)
                && UserLevelConstants.CONSUMPTION_TRIGGER_COMPLETE.equals(level.getConsumptionTriggerType());
    }

    private boolean levelRequiresOrderCountOnPaid(SystemUserLevel level) {
        return levelRequiresOrderCount(level)
                && UserLevelConstants.ORDER_COUNT_TRIGGER_PAID.equals(
                ObjectUtil.defaultIfNull(level.getOrderCountTriggerType(), UserLevelConstants.ORDER_COUNT_TRIGGER_PAID));
    }

    private boolean levelRequiresOrderCountOnComplete(SystemUserLevel level) {
        return levelRequiresOrderCount(level)
                && UserLevelConstants.ORDER_COUNT_TRIGGER_COMPLETE.equals(level.getOrderCountTriggerType());
    }

}

