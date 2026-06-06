package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.system.SystemUserLevelBrokerage;
import com.zbkj.common.request.SystemUserLevelBrokerageRequest;
import com.zbkj.service.dao.SystemUserLevelBrokerageDao;
import com.zbkj.service.service.SystemUserLevelBrokerageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会员等级返佣配置 Service 实现
 */
@Service
public class SystemUserLevelBrokerageServiceImpl extends ServiceImpl<SystemUserLevelBrokerageDao, SystemUserLevelBrokerage>
        implements SystemUserLevelBrokerageService {

    @Resource
    private SystemUserLevelBrokerageDao dao;

    @Override
    public SystemUserLevelBrokerage getByLevelId(Integer levelId) {
        if (ObjectUtil.isNull(levelId) || levelId <= 0) {
            return null;
        }
        LambdaQueryWrapper<SystemUserLevelBrokerage> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemUserLevelBrokerage::getLevelId, levelId);
        lqw.eq(SystemUserLevelBrokerage::getIsDel, false);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    @Override
    public Map<Integer, SystemUserLevelBrokerage> mapByLevelIds(List<Integer> levelIds) {
        if (CollUtil.isEmpty(levelIds)) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<SystemUserLevelBrokerage> lqw = Wrappers.lambdaQuery();
        lqw.in(SystemUserLevelBrokerage::getLevelId, levelIds);
        lqw.eq(SystemUserLevelBrokerage::getIsDel, false);
        return dao.selectList(lqw).stream()
                .collect(Collectors.toMap(SystemUserLevelBrokerage::getLevelId, item -> item, (a, b) -> a));
    }

    @Override
    public Boolean saveOrUpdateByLevelId(Integer levelId, SystemUserLevelBrokerageRequest request) {
        SystemUserLevelBrokerage brokerage = buildBrokerageEntity(levelId, request);
        SystemUserLevelBrokerage exist = getByLevelId(levelId);
        if (ObjectUtil.isNull(exist)) {
            brokerage.setCreateTime(DateUtil.date());
            brokerage.setUpdateTime(DateUtil.date());
            return save(brokerage);
        }
        brokerage.setId(exist.getId());
        brokerage.setCreateTime(exist.getCreateTime());
        brokerage.setUpdateTime(DateUtil.date());
        return updateById(brokerage);
    }

    @Override
    public Boolean deleteByLevelId(Integer levelId) {
        LambdaUpdateWrapper<SystemUserLevelBrokerage> luw = Wrappers.lambdaUpdate();
        luw.set(SystemUserLevelBrokerage::getIsDel, true);
        luw.set(SystemUserLevelBrokerage::getUpdateTime, DateUtil.date());
        luw.eq(SystemUserLevelBrokerage::getLevelId, levelId);
        luw.eq(SystemUserLevelBrokerage::getIsDel, false);
        return update(luw);
    }

    private SystemUserLevelBrokerage buildBrokerageEntity(Integer levelId, SystemUserLevelBrokerageRequest request) {
        SystemUserLevelBrokerage brokerage = new SystemUserLevelBrokerage();
        brokerage.setLevelId(levelId);
        if (ObjectUtil.isNull(request)) {
            brokerage.setSelfBrokerageRate(0);
            brokerage.setBrokerageRateOne(0);
            brokerage.setBrokerageRateTwo(0);
        } else {
            BeanUtils.copyProperties(request, brokerage);
            brokerage.setSelfBrokerageRate(ObjectUtil.defaultIfNull(request.getSelfBrokerageRate(), 0));
            brokerage.setBrokerageRateOne(ObjectUtil.defaultIfNull(request.getBrokerageRateOne(), 0));
            brokerage.setBrokerageRateTwo(ObjectUtil.defaultIfNull(request.getBrokerageRateTwo(), 0));
        }
        brokerage.setIsDel(false);
        return brokerage;
    }
}
