package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.request.SystemTeamLevelConfigRequest;
import com.zbkj.common.request.TeamBrokerageManageRequest;
import com.zbkj.service.dao.SystemTeamLevelConfigDao;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.SystemTeamLevelConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 团队等级配置 Service 实现
 */
@Service
public class SystemTeamLevelConfigServiceImpl extends ServiceImpl<SystemTeamLevelConfigDao, SystemTeamLevelConfig>
        implements SystemTeamLevelConfigService {

    @Resource
    private SystemTeamLevelConfigDao dao;

    @Lazy
    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public List<SystemTeamLevelConfig> getList() {
        LambdaQueryWrapper<SystemTeamLevelConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemTeamLevelConfig::getIsDel, false);
        lqw.orderByAsc(SystemTeamLevelConfig::getTeamLevelId);
        return dao.selectList(lqw);
    }

    @Override
    public SystemTeamLevelConfig getByTeamLevelId(Integer teamLevelId) {
        if (ObjectUtil.isNull(teamLevelId) || teamLevelId <= 0) {
            return null;
        }
        LambdaQueryWrapper<SystemTeamLevelConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemTeamLevelConfig::getTeamLevelId, teamLevelId);
        lqw.eq(SystemTeamLevelConfig::getIsDel, false);
        lqw.last(" limit 1");
        return dao.selectOne(lqw);
    }

    @Override
    public Map<Integer, SystemTeamLevelConfig> mapByTeamLevelIds(List<Integer> teamLevelIds) {
        if (CollUtil.isEmpty(teamLevelIds)) {
            return new HashMap<>();
        }
        LambdaQueryWrapper<SystemTeamLevelConfig> lqw = Wrappers.lambdaQuery();
        lqw.in(SystemTeamLevelConfig::getTeamLevelId, teamLevelIds);
        lqw.eq(SystemTeamLevelConfig::getIsDel, false);
        return dao.selectList(lqw).stream()
                .collect(Collectors.toMap(SystemTeamLevelConfig::getTeamLevelId, item -> item, (a, b) -> a));
    }

    @Override
    public Boolean saveOrUpdateByTeamLevelId(Integer teamLevelId, SystemTeamLevelConfigRequest request) {
        validateTeamRateByGrade(teamLevelId, ObjectUtil.isNull(request) ? 0
                : ObjectUtil.defaultIfNull(request.getTeamBrokerageRate(), 0));

        SystemTeamLevelConfig config = buildConfigEntity(teamLevelId, request);
        SystemTeamLevelConfig exist = getByTeamLevelId(teamLevelId);
        if (ObjectUtil.isNull(exist)) {
            config.setCreateTime(DateUtil.date());
            config.setUpdateTime(DateUtil.date());
            return save(config);
        }
        config.setId(exist.getId());
        config.setCreateTime(exist.getCreateTime());
        config.setUpdateTime(DateUtil.date());
        return updateById(config);
    }

    @Override
    public Boolean deleteByTeamLevelId(Integer teamLevelId) {
        LambdaUpdateWrapper<SystemTeamLevelConfig> luw = Wrappers.lambdaUpdate();
        luw.set(SystemTeamLevelConfig::getIsDel, true);
        luw.set(SystemTeamLevelConfig::getUpdateTime, DateUtil.date());
        luw.eq(SystemTeamLevelConfig::getTeamLevelId, teamLevelId);
        luw.eq(SystemTeamLevelConfig::getIsDel, false);
        return update(luw);
    }

    @Override
    public TeamBrokerageManageRequest getManageInfo() {
        TeamBrokerageManageRequest response = new TeamBrokerageManageRequest();
        String status = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_STATUS);
        String maxDepth = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_MAX_DEPTH);
        response.setTeamBrokerageStatus(parseIntOrDefault(status, 0));
        response.setTeamBrokerageMaxDepth(parseIntOrDefault(maxDepth, 0));
        String creditTiming = systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_CREDIT_TIMING);
        response.setTeamBrokerageCreditTiming(parseIntOrDefault(creditTiming, 1));
        return response;
    }

    @Override
    public Boolean setManageInfo(TeamBrokerageManageRequest request) {
        systemConfigService.updateOrSaveValueByName(
                SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_STATUS,
                request.getTeamBrokerageStatus().toString());
        systemConfigService.updateOrSaveValueByName(
                SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_MAX_DEPTH,
                request.getTeamBrokerageMaxDepth().toString());
        Integer creditTiming = ObjectUtil.defaultIfNull(request.getTeamBrokerageCreditTiming(), 1);
        systemConfigService.updateOrSaveValueByName(
                SysConfigConstants.CONFIG_KEY_TEAM_BROKERAGE_CREDIT_TIMING,
                creditTiming.toString());
        return Boolean.TRUE;
    }

    private SystemTeamLevelConfig buildConfigEntity(Integer teamLevelId, SystemTeamLevelConfigRequest request) {
        SystemTeamLevelConfig config = new SystemTeamLevelConfig();
        config.setTeamLevelId(teamLevelId);
        if (ObjectUtil.isNull(request)) {
            config.setTeamBrokerageRate(0);
            config.setPeerAwardRate(0);
        } else {
            BeanUtils.copyProperties(request, config);
            config.setTeamBrokerageRate(ObjectUtil.defaultIfNull(request.getTeamBrokerageRate(), 0));
            config.setPeerAwardRate(ObjectUtil.defaultIfNull(request.getPeerAwardRate(), 0));
        }
        config.setIsDel(false);
        return config;
    }

    private void validateTeamRateByGrade(Integer teamLevelId, Integer teamBrokerageRate) {
        SystemTeamLevel currentLevel = systemTeamLevelService.getById(teamLevelId);
        if (ObjectUtil.isNull(currentLevel) || currentLevel.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
        List<SystemTeamLevel> levelList = systemTeamLevelService.getAllList();
        if (CollUtil.isEmpty(levelList)) {
            return;
        }
        for (SystemTeamLevel level : levelList) {
            if (level.getId().equals(teamLevelId)) {
                continue;
            }
            SystemTeamLevelConfig config = getByTeamLevelId(level.getId());
            if (ObjectUtil.isNull(config)) {
                continue;
            }
            Integer existRate = ObjectUtil.defaultIfNull(config.getTeamBrokerageRate(), 0);
            if (level.getGrade() < currentLevel.getGrade() && existRate > teamBrokerageRate) {
                throw new CrmebException(StrUtil.format("团队极差比例不能低于低等级【{}】的{}%",
                        level.getName(), existRate));
            }
            if (level.getGrade() > currentLevel.getGrade() && existRate < teamBrokerageRate) {
                throw new CrmebException(StrUtil.format("团队极差比例不能高于高等级【{}】的{}%",
                        level.getName(), existRate));
            }
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (StrUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
