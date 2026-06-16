package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.request.SystemTeamLevelRequest;
import com.zbkj.common.request.SystemTeamLevelUpdateShowRequest;
import com.zbkj.common.response.SystemTeamLevelInfoResponse;
import com.zbkj.service.dao.SystemTeamLevelDao;
import com.zbkj.service.service.SystemAttachmentService;
import com.zbkj.service.service.SystemTeamLevelConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 团队等级 Service 实现
 */
@Service
public class SystemTeamLevelServiceImpl extends ServiceImpl<SystemTeamLevelDao, SystemTeamLevel>
        implements SystemTeamLevelService {

    @Resource
    private SystemTeamLevelDao dao;

    @Autowired
    private SystemAttachmentService systemAttachmentService;

    @Autowired
    private SystemTeamLevelConfigService systemTeamLevelConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public List<SystemTeamLevel> getList() {
        return getAllList();
    }

    @Override
    public List<SystemTeamLevel> getAllList() {
        LambdaQueryWrapper<SystemTeamLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemTeamLevel::getIsDel, false);
        lqw.orderByAsc(SystemTeamLevel::getGrade);
        List<SystemTeamLevel> levelList = dao.selectList(lqw);
        fillTeamLevelConfig(levelList);
        return levelList;
    }

    @Override
    public SystemTeamLevelInfoResponse getInfo(Integer id) {
        SystemTeamLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
        SystemTeamLevelInfoResponse response = new SystemTeamLevelInfoResponse();
        response.setLevel(level);
        response.setConfig(systemTeamLevelConfigService.getByTeamLevelId(id));
        return response;
    }

    @Override
    public Boolean create(SystemTeamLevelRequest request) {
        checkLevel(request);
        SystemTeamLevel teamLevel = buildLevelEntity(request);
        teamLevel.setCreateTime(DateUtil.date());
        teamLevel.setUpdateTime(DateUtil.date());
        return transactionTemplate.execute(e -> {
            if (!save(teamLevel)) {
                return Boolean.FALSE;
            }
            systemTeamLevelConfigService.saveOrUpdateByTeamLevelId(teamLevel.getId(), request.getConfig());
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean update(Integer id, SystemTeamLevelRequest request) {
        SystemTeamLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
        request.setId(id);
        checkLevel(request);
        SystemTeamLevel teamLevel = buildLevelEntity(request);
        teamLevel.setId(id);
        teamLevel.setIsShow(level.getIsShow());
        return transactionTemplate.execute(e -> {
            teamLevel.setUpdateTime(DateUtil.date());
            dao.updateById(teamLevel);
            systemTeamLevelConfigService.saveOrUpdateByTeamLevelId(id, request.getConfig());
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean delete(Integer id) {
        SystemTeamLevel level = getById(id);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
        level.setIsDel(true);
        return transactionTemplate.execute(e -> {
            level.setUpdateTime(DateUtil.date());
            dao.updateById(level);
            systemTeamLevelConfigService.deleteByTeamLevelId(id);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean updateShow(SystemTeamLevelUpdateShowRequest request) {
        SystemTeamLevel level = getById(request.getId());
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
        level.setIsShow(request.getIsShow());
        level.setUpdateTime(DateUtil.date());
        return updateById(level);
    }

    @Override
    public List<SystemTeamLevel> getUsableList() {
        LambdaQueryWrapper<SystemTeamLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemTeamLevel::getIsShow, true);
        lqw.eq(SystemTeamLevel::getIsDel, false);
        lqw.orderByAsc(SystemTeamLevel::getGrade);
        return dao.selectList(lqw);
    }

    private SystemTeamLevel buildLevelEntity(SystemTeamLevelRequest request) {
        SystemTeamLevel teamLevel = new SystemTeamLevel();
        BeanUtils.copyProperties(request, teamLevel);
        teamLevel.setIcon(systemAttachmentService.clearPrefix(request.getIcon()));
        teamLevel.setSelfOrderAmount(ObjectUtil.defaultIfNull(request.getSelfOrderAmount(), BigDecimal.ZERO));
        teamLevel.setTeamOrderAmount(ObjectUtil.defaultIfNull(request.getTeamOrderAmount(), BigDecimal.ZERO));
        teamLevel.setIsDel(false);
        return teamLevel;
    }

    private void checkLevel(SystemTeamLevelRequest request) {
        LambdaQueryWrapper<SystemTeamLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemTeamLevel::getName, request.getName());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemTeamLevel::getId, request.getId());
        }
        lqw.eq(SystemTeamLevel::getIsDel, false);
        if (ObjectUtil.isNotNull(dao.selectOne(lqw))) {
            throw new CrmebException("团队等级名称重复");
        }

        lqw.clear();
        lqw.eq(SystemTeamLevel::getGrade, request.getGrade());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemTeamLevel::getId, request.getId());
        }
        lqw.eq(SystemTeamLevel::getIsDel, false);
        if (ObjectUtil.isNotNull(dao.selectOne(lqw))) {
            throw new CrmebException("团队等级序号重复");
        }

        if (request.getGrade() > 1) {
            lqw.clear();
            lqw.lt(SystemTeamLevel::getGrade, request.getGrade());
            if (ObjectUtil.isNotNull(request.getId())) {
                lqw.ne(SystemTeamLevel::getId, request.getId());
            }
            lqw.eq(SystemTeamLevel::getIsDel, false);
            lqw.orderByDesc(SystemTeamLevel::getGrade);
            lqw.last(" limit 1");
            SystemTeamLevel prev = dao.selectOne(lqw);
            if (ObjectUtil.isNotNull(prev) && !isThresholdHigher(request, prev)) {
                throw new CrmebException("当前团队等级的升级门槛不能比上一级别的低");
            }
        }

        lqw.clear();
        lqw.gt(SystemTeamLevel::getGrade, request.getGrade());
        if (ObjectUtil.isNotNull(request.getId())) {
            lqw.ne(SystemTeamLevel::getId, request.getId());
        }
        lqw.eq(SystemTeamLevel::getIsDel, false);
        lqw.orderByAsc(SystemTeamLevel::getGrade);
        lqw.last(" limit 1");
        SystemTeamLevel next = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(next) && !isThresholdHigher(next, request)) {
            throw new CrmebException("当前团队等级的升级门槛不能比下一级别的高");
        }
    }

    private boolean isThresholdHigher(SystemTeamLevelRequest request, SystemTeamLevel other) {
        return request.getSelfOrderAmount().compareTo(other.getSelfOrderAmount()) > 0
                && request.getTeamOrderAmount().compareTo(other.getTeamOrderAmount()) > 0;
    }

    private boolean isThresholdHigher(SystemTeamLevel higher, SystemTeamLevelRequest lower) {
        return higher.getSelfOrderAmount().compareTo(lower.getSelfOrderAmount()) > 0
                && higher.getTeamOrderAmount().compareTo(lower.getTeamOrderAmount()) > 0;
    }

    private void fillTeamLevelConfig(List<SystemTeamLevel> levelList) {
        if (CollUtil.isEmpty(levelList)) {
            return;
        }
        List<Integer> levelIds = levelList.stream().map(SystemTeamLevel::getId).collect(Collectors.toList());
        Map<Integer, SystemTeamLevelConfig> configMap = systemTeamLevelConfigService.mapByTeamLevelIds(levelIds);
        levelList.forEach(level -> level.setConfig(configMap.get(level.getId())));
    }
}
