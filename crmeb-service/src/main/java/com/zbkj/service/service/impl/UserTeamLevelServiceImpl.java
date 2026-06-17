package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserTeamLevel;
import com.zbkj.common.model.user.UserTeamLevelStat;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserTeamLevelRecordResponse;
import com.zbkj.common.response.UserTeamLevelUserResponse;
import com.zbkj.service.dao.UserTeamLevelDao;
import com.zbkj.service.dao.UserTeamLevelStatDao;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import com.zbkj.service.service.UserService;
import com.zbkj.service.service.UserTeamLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户团队等级实现：统计自购/团队订单金额（按支付/完成）并同步团队等级
 */
@Service
public class UserTeamLevelServiceImpl extends ServiceImpl<UserTeamLevelDao, UserTeamLevel> implements UserTeamLevelService {

    @Resource
    private UserTeamLevelDao userTeamLevelDao;

    @Resource
    private UserTeamLevelStatDao userTeamLevelStatDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Boolean processTeamLevelOnOrderPaid(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return Boolean.TRUE;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.TRUE;
        }
        return transactionTemplate.execute(e -> {
            List<Integer> affectedUids = applyPaidStats(storeOrder, amount);
            syncTeamLevels(affectedUids);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean processTeamLevelOnOrderComplete(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder)) {
            return Boolean.TRUE;
        }
        // status==3 已完成
        if (!Integer.valueOf(3).equals(storeOrder.getStatus())) {
            return Boolean.TRUE;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.TRUE;
        }
        return transactionTemplate.execute(e -> {
            List<Integer> affectedUids = applyCompleteStats(storeOrder, amount);
            syncTeamLevels(affectedUids);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean rollbackTeamLevelOnRefund(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder)) {
            return Boolean.TRUE;
        }
        if (!Integer.valueOf(2).equals(storeOrder.getRefundStatus())) {
            // 只在已退款时回滚
            return Boolean.TRUE;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.TRUE;
        }
        // 退款一定是已支付订单
        boolean wasPaid = Boolean.TRUE.equals(storeOrder.getPaid());
        boolean wasComplete = Integer.valueOf(3).equals(storeOrder.getStatus());
        return transactionTemplate.execute(e -> {
            Set<Integer> affected = new HashSet<>();
            if (wasPaid) {
                affected.addAll(rollbackPaidStats(storeOrder, amount));
            }
            if (wasComplete) {
                affected.addAll(rollbackCompleteStats(storeOrder, amount));
            }
            syncTeamLevels(affected.stream().collect(Collectors.toList()));
            return Boolean.TRUE;
        });
    }

    @Override
    public SystemTeamLevel resolveMatchedTeamLevel(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        List<SystemTeamLevel> list = systemTeamLevelService.getUsableList();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        UserTeamLevelStat stat = getOrInitStat(user.getUid());
        return list.stream()
                .filter(level -> meetsTeamLevelCondition(level, stat))
                .max((a, b) -> ObjectUtil.defaultIfNull(a.getGrade(), 0) - ObjectUtil.defaultIfNull(b.getGrade(), 0))
                .orElse(null);
    }

    @Override
    public Boolean syncTeamLevels(List<Integer> uids) {
        if (CollUtil.isEmpty(uids)) {
            return Boolean.TRUE;
        }
        for (Integer uid : uids.stream().distinct().collect(Collectors.toList())) {
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                continue;
            }
            SystemTeamLevel matched = resolveMatchedTeamLevel(user);
            Integer matchedLevelId = ObjectUtil.isNull(matched) ? 0 : matched.getId();
            Integer currentLevelId = ObjectUtil.defaultIfNull(user.getTeamLevel(), 0);
            if (currentLevelId.equals(matchedLevelId)) {
                continue;
            }
            // 更新用户当前团队等级
            User updateUser = new User();
            updateUser.setUid(uid);
            updateUser.setTeamLevel(matchedLevelId);
            updateUser.setUpdateTime(DateUtil.date());
            userService.updateById(updateUser);

            // 记录变更（简单追加记录）
            UserTeamLevel record = new UserTeamLevel();
            record.setUid(uid);
            record.setTeamLevelId(matchedLevelId);
            record.setGrade(ObjectUtil.isNull(matched) ? 0 : ObjectUtil.defaultIfNull(matched.getGrade(), 0));
            record.setStatus(true);
            record.setRemind(false);
            record.setIsDel(false);
            record.setMark(ObjectUtil.isNull(matched) ? "团队等级清空/降级" : "团队等级变更");
            record.setCreateTime(DateUtil.date());
            record.setUpdateTime(DateUtil.date());
            userTeamLevelDao.insert(record);
        }
        return Boolean.TRUE;
    }

    @Override
    public PageInfo<UserTeamLevelUserResponse> getTeamUserPage(String keywords, Integer teamLevelId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        return new PageInfo<>(userTeamLevelDao.getTeamUserPage(keywords, teamLevelId));
    }

    @Override
    public PageInfo<UserTeamLevelRecordResponse> getTeamRecordPage(String keywords, Integer teamLevelId, Integer status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        return new PageInfo<>(userTeamLevelDao.getTeamRecordPage(keywords, teamLevelId, status));
    }

    private boolean meetsTeamLevelCondition(SystemTeamLevel level, UserTeamLevelStat stat) {
        if (ObjectUtil.isNull(level) || ObjectUtil.isNull(stat)) {
            return false;
        }
        BigDecimal selfValue = selectSelfAmount(level, stat);
        BigDecimal teamValue = selectTeamAmount(level, stat);
        BigDecimal selfThreshold = ObjectUtil.defaultIfNull(level.getSelfOrderAmount(), BigDecimal.ZERO);
        BigDecimal teamThreshold = ObjectUtil.defaultIfNull(level.getTeamOrderAmount(), BigDecimal.ZERO);
        return selfValue.compareTo(selfThreshold) >= 0 && teamValue.compareTo(teamThreshold) >= 0;
    }

    private BigDecimal selectSelfAmount(SystemTeamLevel level, UserTeamLevelStat stat) {
        Integer trigger = ObjectUtil.defaultIfNull(level.getSelfOrderTriggerType(), 2);
        if (trigger == 1) {
            return ObjectUtil.defaultIfNull(stat.getSelfPaidAmount(), BigDecimal.ZERO);
        }
        return ObjectUtil.defaultIfNull(stat.getSelfCompleteAmount(), BigDecimal.ZERO);
    }

    private BigDecimal selectTeamAmount(SystemTeamLevel level, UserTeamLevelStat stat) {
        Integer trigger = ObjectUtil.defaultIfNull(level.getTeamOrderTriggerType(), 2);
        if (trigger == 1) {
            return ObjectUtil.defaultIfNull(stat.getTeamPaidAmount(), BigDecimal.ZERO);
        }
        return ObjectUtil.defaultIfNull(stat.getTeamCompleteAmount(), BigDecimal.ZERO);
    }

    private List<Integer> applyPaidStats(StoreOrder storeOrder, BigDecimal amount) {
        Set<Integer> affected = new HashSet<>();
        // buyer self paid
        affected.add(storeOrder.getUid());
        addSelfPaid(storeOrder.getUid(), amount);
        // uplines team paid
        affected.addAll(addTeamPaidToUplines(storeOrder.getUid(), amount));
        return affected.stream().collect(Collectors.toList());
    }

    private List<Integer> applyCompleteStats(StoreOrder storeOrder, BigDecimal amount) {
        Set<Integer> affected = new HashSet<>();
        affected.add(storeOrder.getUid());
        addSelfComplete(storeOrder.getUid(), amount);
        affected.addAll(addTeamCompleteToUplines(storeOrder.getUid(), amount));
        return affected.stream().collect(Collectors.toList());
    }

    private List<Integer> rollbackPaidStats(StoreOrder storeOrder, BigDecimal amount) {
        Set<Integer> affected = new HashSet<>();
        affected.add(storeOrder.getUid());
        addSelfPaid(storeOrder.getUid(), amount.negate());
        affected.addAll(addTeamPaidToUplines(storeOrder.getUid(), amount.negate()));
        return affected.stream().collect(Collectors.toList());
    }

    private List<Integer> rollbackCompleteStats(StoreOrder storeOrder, BigDecimal amount) {
        Set<Integer> affected = new HashSet<>();
        affected.add(storeOrder.getUid());
        addSelfComplete(storeOrder.getUid(), amount.negate());
        affected.addAll(addTeamCompleteToUplines(storeOrder.getUid(), amount.negate()));
        return affected.stream().collect(Collectors.toList());
    }

    private void addSelfPaid(Integer uid, BigDecimal delta) {
        UserTeamLevelStat stat = getOrInitStat(uid);
        stat.setSelfPaidAmount(nonNegative(ObjectUtil.defaultIfNull(stat.getSelfPaidAmount(), BigDecimal.ZERO).add(delta)));
        stat.setUpdateTime(DateUtil.date());
        userTeamLevelStatDao.updateById(stat);
    }

    private void addSelfComplete(Integer uid, BigDecimal delta) {
        UserTeamLevelStat stat = getOrInitStat(uid);
        stat.setSelfCompleteAmount(nonNegative(ObjectUtil.defaultIfNull(stat.getSelfCompleteAmount(), BigDecimal.ZERO).add(delta)));
        stat.setUpdateTime(DateUtil.date());
        userTeamLevelStatDao.updateById(stat);
    }

    private Set<Integer> addTeamPaidToUplines(Integer buyerUid, BigDecimal delta) {
        return addTeamToUplines(buyerUid, delta, true);
    }

    private Set<Integer> addTeamCompleteToUplines(Integer buyerUid, BigDecimal delta) {
        return addTeamToUplines(buyerUid, delta, false);
    }

    private Set<Integer> addTeamToUplines(Integer buyerUid, BigDecimal delta, boolean isPaid) {
        Set<Integer> affected = new HashSet<>();
        User buyer = userService.getById(buyerUid);
        if (ObjectUtil.isNull(buyer) || ObjectUtil.defaultIfNull(buyer.getSpreadUid(), 0) <= 0) {
            return affected;
        }
        Integer currentUid = buyer.getSpreadUid();
        Set<Integer> visited = new HashSet<>();
        int maxDepth = getMaxDepth();
        int depth = 0;
        while (ObjectUtil.isNotNull(currentUid) && currentUid > 0) {
            if (!visited.add(currentUid)) {
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                break;
            }
            affected.add(currentUid);
            UserTeamLevelStat stat = getOrInitStat(currentUid);
            if (isPaid) {
                stat.setTeamPaidAmount(nonNegative(ObjectUtil.defaultIfNull(stat.getTeamPaidAmount(), BigDecimal.ZERO).add(delta)));
            } else {
                stat.setTeamCompleteAmount(nonNegative(ObjectUtil.defaultIfNull(stat.getTeamCompleteAmount(), BigDecimal.ZERO).add(delta)));
            }
            stat.setUpdateTime(DateUtil.date());
            userTeamLevelStatDao.updateById(stat);

            User parent = userService.getById(currentUid);
            if (ObjectUtil.isNull(parent)) {
                break;
            }
            currentUid = parent.getSpreadUid();
        }
        return affected;
    }

    private int getMaxDepth() {
        // 复用系统配置 team_brokerage_max_depth，0=不限（避免再造配置）
        String maxDepthStr = systemConfigService.getValueByKey("team_brokerage_max_depth");
        try {
            int v = Integer.parseInt(ObjectUtil.defaultIfNull(maxDepthStr, "0"));
            // 防御：无限也别真无限，避免异常链条拖垮
            return v <= 0 ? 0 : Math.min(v, 200);
        } catch (Exception e) {
            return 0;
        }
    }

    private UserTeamLevelStat getOrInitStat(Integer uid) {
        LambdaQueryWrapper<UserTeamLevelStat> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserTeamLevelStat::getUid, uid);
        lqw.last(" limit 1");
        UserTeamLevelStat stat = userTeamLevelStatDao.selectOne(lqw);
        if (ObjectUtil.isNotNull(stat)) {
            // 兼容旧数据空值
            stat.setSelfPaidAmount(ObjectUtil.defaultIfNull(stat.getSelfPaidAmount(), BigDecimal.ZERO));
            stat.setSelfCompleteAmount(ObjectUtil.defaultIfNull(stat.getSelfCompleteAmount(), BigDecimal.ZERO));
            stat.setTeamPaidAmount(ObjectUtil.defaultIfNull(stat.getTeamPaidAmount(), BigDecimal.ZERO));
            stat.setTeamCompleteAmount(ObjectUtil.defaultIfNull(stat.getTeamCompleteAmount(), BigDecimal.ZERO));
            return stat;
        }
        UserTeamLevelStat init = new UserTeamLevelStat();
        init.setUid(uid);
        init.setSelfPaidAmount(BigDecimal.ZERO);
        init.setSelfCompleteAmount(BigDecimal.ZERO);
        init.setTeamPaidAmount(BigDecimal.ZERO);
        init.setTeamCompleteAmount(BigDecimal.ZERO);
        init.setCreateTime(DateUtil.date());
        init.setUpdateTime(DateUtil.date());
        userTeamLevelStatDao.insert(init);
        return init;
    }

    private BigDecimal nonNegative(BigDecimal v) {
        if (ObjectUtil.isNull(v)) {
            return BigDecimal.ZERO;
        }
        return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
    }
}

