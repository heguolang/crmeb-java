package com.zbkj.admin.controller;

import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.request.SystemTeamLevelRequest;
import com.zbkj.common.request.SystemTeamLevelUpdateShowRequest;
import com.zbkj.common.response.SystemTeamLevelInfoResponse;
import com.zbkj.common.result.CommonResult;
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
 * 团队等级 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/team/level")
@Api(tags = "设置 -- 团队等级")
public class SystemTeamLevelController {

    @Autowired
    private SystemTeamLevelService systemTeamLevelService;

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "团队等级列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<SystemTeamLevel>> getList() {
        return CommonResult.success(systemTeamLevelService.getList());
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "全部团队等级列表（不分页）")
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public CommonResult<List<SystemTeamLevel>> getAllList() {
        return CommonResult.success(systemTeamLevelService.getAllList());
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:list')")
    @ApiOperation(value = "团队等级详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemTeamLevelInfoResponse> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(systemTeamLevelService.getInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:save')")
    @ApiOperation(value = "新增团队等级", notes = "可同时配置团队极差比例、平级奖比例；升级判定逻辑暂未接入")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<Object> save(@RequestBody @Validated SystemTeamLevelRequest request) {
        if (systemTeamLevelService.create(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:delete')")
    @ApiOperation(value = "删除团队等级")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<Object> delete(@PathVariable(value = "id") Integer id) {
        if (systemTeamLevelService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:update')")
    @ApiOperation(value = "更新团队等级")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Object> update(@PathVariable(value = "id") Integer id,
                                       @RequestBody @Validated SystemTeamLevelRequest request) {
        if (systemTeamLevelService.update(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:use')")
    @ApiOperation(value = "团队等级使用/禁用")
    @RequestMapping(value = "/use", method = RequestMethod.POST)
    public CommonResult<Object> use(@RequestBody @Validated SystemTeamLevelUpdateShowRequest request) {
        if (systemTeamLevelService.updateShow(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
