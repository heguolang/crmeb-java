package com.zbkj.admin.controller;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserTeamLevelRecordResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserTeamLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 团队等级 - 变更记录 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/team/level")
@Api(tags = "分销 -- 团队等级变更记录")
public class SystemTeamLevelRecordController {

    @Autowired
    private UserTeamLevelService userTeamLevelService;

    /**
     * 团队等级变更记录列表
     */
    @PreAuthorize("hasAuthority('admin:system:team:level:record:list')")
    @ApiOperation(value = "团队等级变更记录列表")
    @GetMapping("/record/list")
    public CommonResult<PageInfo<UserTeamLevelRecordResponse>> recordList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "teamLevelId", required = false) Integer teamLevelId,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        PageParamRequest pageParamRequest = new PageParamRequest();
        pageParamRequest.setPage(page);
        pageParamRequest.setLimit(limit);
        PageInfo<UserTeamLevelRecordResponse> pageInfo =
                userTeamLevelService.getTeamRecordPage(keywords, teamLevelId, status, pageParamRequest);
        return CommonResult.success(pageInfo);
    }
}

