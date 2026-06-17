package com.zbkj.admin.controller;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.UserTeamLevelUserResponse;
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
 * 团队等级 - 团队关联用户 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/team/level")
@Api(tags = "分销 -- 团队关联用户")
public class SystemTeamLevelUserController {

    @Autowired
    private UserTeamLevelService userTeamLevelService;

    /**
     * 团队关联用户列表
     */
    @PreAuthorize("hasAuthority('admin:system:team:level:user:list')")
    @ApiOperation(value = "团队关联用户列表")
    @GetMapping("/user/list")
    public CommonResult<PageInfo<UserTeamLevelUserResponse>> userList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "teamLevelId", required = false) Integer teamLevelId
    ) {
        PageParamRequest pageParamRequest = new PageParamRequest();
        pageParamRequest.setPage(page);
        pageParamRequest.setLimit(limit);
        PageInfo<UserTeamLevelUserResponse> pageInfo =
                userTeamLevelService.getTeamUserPage(keywords, teamLevelId, pageParamRequest);
        return CommonResult.success(pageInfo);
    }
}

