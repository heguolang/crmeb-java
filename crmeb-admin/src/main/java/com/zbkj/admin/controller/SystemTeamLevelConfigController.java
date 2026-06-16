package com.zbkj.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import com.zbkj.common.request.SystemTeamLevelConfigRequest;
import com.zbkj.common.request.SystemTeamLevelConfigSaveRequest;
import com.zbkj.common.request.TeamBrokerageManageRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.SystemTeamLevelConfigService;
import com.zbkj.service.service.SystemTeamLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团队等级配置 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/team/level/config")
@Api(tags = "设置 -- 团队等级配置")
public class SystemTeamLevelConfigController {

    @Autowired
    private SystemTeamLevelConfigService systemTeamLevelConfigService;

    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "团队奖全局配置获取")
    @RequestMapping(value = "/manage/get", method = RequestMethod.GET)
    public CommonResult<TeamBrokerageManageRequest> getManageInfo() {
        return CommonResult.success(systemTeamLevelConfigService.getManageInfo());
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:update')")
    @ApiOperation(value = "团队奖全局配置保存")
    @RequestMapping(value = "/manage/set", method = RequestMethod.POST)
    public CommonResult<String> setManageInfo(@RequestBody @Validated TeamBrokerageManageRequest request) {
        if (systemTeamLevelConfigService.setManageInfo(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "团队等级配置列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<SystemTeamLevelConfig>> getList() {
        return CommonResult.success(systemTeamLevelConfigService.getList());
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "团队等级配置详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemTeamLevelConfig> info(@RequestParam(value = "teamLevelId") Integer teamLevelId) {
        validateTeamLevelExists(teamLevelId);
        return CommonResult.success(systemTeamLevelConfigService.getByTeamLevelId(teamLevelId));
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:update')")
    @ApiOperation(value = "保存团队等级配置")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated SystemTeamLevelConfigSaveRequest request) {
        validateTeamLevelExists(request.getTeamLevelId());
        SystemTeamLevelConfigRequest configRequest = new SystemTeamLevelConfigRequest();
        configRequest.setTeamBrokerageRate(request.getTeamBrokerageRate());
        configRequest.setPeerAwardRate(request.getPeerAwardRate());
        if (systemTeamLevelConfigService.saveOrUpdateByTeamLevelId(request.getTeamLevelId(), configRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:delete')")
    @ApiOperation(value = "删除团队等级配置")
    @RequestMapping(value = "/delete/{teamLevelId}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "teamLevelId") Integer teamLevelId) {
        validateTeamLevelExists(teamLevelId);
        if (systemTeamLevelConfigService.deleteByTeamLevelId(teamLevelId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    private void validateTeamLevelExists(Integer teamLevelId) {
        SystemTeamLevel level = systemTeamLevelService.getById(teamLevelId);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("团队等级不存在");
        }
    }
}
