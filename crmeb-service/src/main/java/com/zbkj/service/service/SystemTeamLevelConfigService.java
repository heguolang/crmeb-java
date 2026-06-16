package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.request.SystemTeamLevelConfigRequest;
import com.zbkj.common.request.TeamBrokerageManageRequest;

import java.util.List;
import java.util.Map;

/**
 * 团队等级配置 Service
 */
public interface SystemTeamLevelConfigService extends IService<SystemTeamLevelConfig> {

    List<SystemTeamLevelConfig> getList();

    SystemTeamLevelConfig getByTeamLevelId(Integer teamLevelId);

    Map<Integer, SystemTeamLevelConfig> mapByTeamLevelIds(List<Integer> teamLevelIds);

    Boolean saveOrUpdateByTeamLevelId(Integer teamLevelId, SystemTeamLevelConfigRequest request);

    Boolean deleteByTeamLevelId(Integer teamLevelId);

    TeamBrokerageManageRequest getManageInfo();

    Boolean setManageInfo(TeamBrokerageManageRequest request);
}
