package com.zbkj.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.TeamBrokerageRecordRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserBrokerageRecordService;
import com.zbkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 团队奖资金记录
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/team/level")
@Api(tags = "分销 -- 团队奖资金记录")
public class SystemTeamBrokerageRecordController {

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('admin:system:team:level:brokerage:record')")
    @ApiOperation(value = "团队奖资金记录列表")
    @GetMapping("/brokerage/record")
    public CommonResult<CommonPage<UserBrokerageRecord>> brokerageRecord(
            @Validated TeamBrokerageRecordRequest request,
            @Validated PageParamRequest pageParamRequest) {
        PageInfo<UserBrokerageRecord> pageInfo = userBrokerageRecordService.getTeamBrokerageAdminList(request, pageParamRequest);
        List<UserBrokerageRecord> list = pageInfo.getList();
        if (CollUtil.isNotEmpty(list)) {
            List<Integer> uidList = list.stream().map(UserBrokerageRecord::getUid).distinct().collect(Collectors.toList());
            HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
            list.forEach(e -> {
                String name = "-";
                if (ObjectUtil.isNotNull(userMap.get(e.getUid()))) {
                    name = userMap.get(e.getUid()).getNickname();
                }
                e.setUserName(name);
            });
            pageInfo.setList(list);
        }
        return CommonResult.success(CommonPage.restPage(pageInfo));
    }
}
