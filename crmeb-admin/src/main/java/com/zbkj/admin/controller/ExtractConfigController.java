package com.zbkj.admin.controller;

import com.zbkj.common.request.ExtractConfigRequest;
import com.zbkj.common.response.ExtractConfigResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.ExtractConfigService;
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
 * 提现设置
 */
@Slf4j
@RestController
@RequestMapping("api/admin/finance/extract")
@Api(tags = "财务 -- 提现设置")
public class ExtractConfigController {

    @Autowired
    private ExtractConfigService extractConfigService;

    @PreAuthorize("hasAuthority('admin:finance:extract:config')")
    @ApiOperation(value = "获取提现设置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<ExtractConfigResponse> getConfig() {
        return CommonResult.success(extractConfigService.getConfig());
    }

    @PreAuthorize("hasAuthority('admin:finance:extract:config')")
    @ApiOperation(value = "保存提现设置")
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public CommonResult<Boolean> saveConfig(@RequestBody @Validated ExtractConfigRequest request) {
        return CommonResult.success(extractConfigService.saveConfig(request));
    }
}
