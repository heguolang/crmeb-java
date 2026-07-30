package com.zbkj.admin.controller;

import com.zbkj.common.request.BalanceFeatureConfigRequest;
import com.zbkj.common.response.BalanceFeatureConfigResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.BalanceFeatureConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 余额功能设置
 */
@Slf4j
@RestController
@RequestMapping("api/admin/finance/balance/feature")
@Api(tags = "财务 -- 余额功能设置")
public class BalanceFeatureConfigController {

    @Autowired
    private BalanceFeatureConfigService balanceFeatureConfigService;

    @PreAuthorize("hasAuthority('admin:finance:balance:feature:config')")
    @ApiOperation(value = "获取余额功能设置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<BalanceFeatureConfigResponse> getConfig() {
        return CommonResult.success(balanceFeatureConfigService.getConfig());
    }

    @PreAuthorize("hasAuthority('admin:finance:balance:feature:config')")
    @ApiOperation(value = "保存余额功能设置")
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public CommonResult<Boolean> saveConfig(@RequestBody @Validated BalanceFeatureConfigRequest request) {
        return CommonResult.success(balanceFeatureConfigService.saveConfig(request));
    }
}
