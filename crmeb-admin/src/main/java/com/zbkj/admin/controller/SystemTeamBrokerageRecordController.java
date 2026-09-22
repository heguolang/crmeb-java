package com.zbkj.admin.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.TeamBrokerageRecordRequest;
import com.zbkj.common.response.TeamBrokerageAuditItemResponse;
import com.zbkj.common.response.TeamBrokerageReissueResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.TeamBrokerageService;
import com.zbkj.service.service.UserBrokerageRecordService;
import com.zbkj.service.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private TeamBrokerageService teamBrokerageService;

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
            fillUserName(list);
            pageInfo.setList(list);
        }
        return CommonResult.success(CommonPage.restPage(pageInfo));
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:brokerage:record')")
    @ApiOperation(value = "团队奖资金记录统计汇总")
    @GetMapping("/brokerage/record/stats")
    public CommonResult<Map<String, Object>> brokerageRecordStats(@Validated TeamBrokerageRecordRequest request) {
        return CommonResult.success(userBrokerageRecordService.getTeamBrokerageAdminStats(request));
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:brokerage:record')")
    @ApiOperation(value = "团队奖漏发补发（按订单号重放计算并补齐缺失记录）")
    @PostMapping("/brokerage/record/reissue/{orderNo}")
    public CommonResult<TeamBrokerageReissueResponse> brokerageRecordReissue(@PathVariable String orderNo) {
        TeamBrokerageReissueResponse response = teamBrokerageService.reissueMissingByOrderNo(orderNo);
        if (response != null && CollUtil.isNotEmpty(response.getRecords())) {
            fillUserName(response.getRecords());
        }
        return CommonResult.success(response);
    }

    @PreAuthorize("hasAuthority('admin:system:team:level:brokerage:record')")
    @ApiOperation(value = "团队奖漏发检测（只读扫描，列出有漏发的订单）")
    @GetMapping("/brokerage/record/audit")
    public CommonResult<List<TeamBrokerageAuditItemResponse>> brokerageRecordAudit(
            @ApiParam(value = "支付开始时间 yyyy-MM-dd") @RequestParam(required = false) String startTime,
            @ApiParam(value = "支付结束时间 yyyy-MM-dd") @RequestParam(required = false) String endTime,
            @ApiParam(value = "最多扫描订单数，默认300，上限1000") @RequestParam(required = false) Integer limit) {
        List<TeamBrokerageAuditItemResponse> list = teamBrokerageService.auditMissingTeamBrokerage(startTime, endTime, limit);
        if (CollUtil.isNotEmpty(list)) {
            List<Integer> uidList = list.stream().map(TeamBrokerageAuditItemResponse::getUid)
                    .filter(ObjectUtil::isNotNull).distinct().collect(Collectors.toList());
            HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
            list.forEach(e -> {
                User user = userMap.get(e.getUid());
                e.setUserName(ObjectUtil.isNotNull(user) ? user.getNickname() : "-");
            });
        }
        return CommonResult.success(list);
    }

    /**
     * 填充用户昵称
     */
    private void fillUserName(List<UserBrokerageRecord> list) {
        List<Integer> uidList = list.stream().map(UserBrokerageRecord::getUid).distinct().collect(Collectors.toList());
        HashMap<Integer, User> userMap = userService.getMapListInUid(uidList);
        list.forEach(e -> {
            String name = "-";
            if (ObjectUtil.isNotNull(userMap.get(e.getUid()))) {
                name = userMap.get(e.getUid()).getNickname();
            }
            e.setUserName(name);
        });
    }
}
