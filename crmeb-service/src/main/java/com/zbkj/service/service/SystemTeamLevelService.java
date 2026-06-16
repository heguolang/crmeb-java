package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.request.SystemTeamLevelRequest;
import com.zbkj.common.request.SystemTeamLevelUpdateShowRequest;
import com.zbkj.common.response.SystemTeamLevelInfoResponse;

import java.util.List;

/**
 * 团队等级 Service
 */
public interface SystemTeamLevelService extends IService<SystemTeamLevel> {

    List<SystemTeamLevel> getList();

    List<SystemTeamLevel> getAllList();

    SystemTeamLevelInfoResponse getInfo(Integer id);

    Boolean create(SystemTeamLevelRequest request);

    Boolean update(Integer id, SystemTeamLevelRequest request);

    Boolean delete(Integer id);

    Boolean updateShow(SystemTeamLevelUpdateShowRequest request);

    List<SystemTeamLevel> getUsableList();
}
