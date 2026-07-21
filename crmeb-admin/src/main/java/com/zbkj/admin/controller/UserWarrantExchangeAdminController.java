package com.zbkj.admin.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserWarrantExchangeSearchRequest;
import com.zbkj.common.response.UserWarrantExchangeResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserWarrantExchangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权证兑换申请
 */
@Slf4j
@RestController
@RequestMapping("api/admin/finance/warrant/exchange")
@Api(tags = "财务 -- 权证兑换")
public class UserWarrantExchangeAdminController {

    @Autowired
    private UserWarrantExchangeService userWarrantExchangeService;

    @PreAuthorize("hasAuthority('admin:finance:warrant:exchange:list')")
    @ApiOperation(value = "权证兑换列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserWarrantExchangeResponse>> getList(
            @Validated UserWarrantExchangeSearchRequest request,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(userWarrantExchangeService.getAdminList(request, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:finance:warrant:exchange:list')")
    @ApiOperation(value = "更新权证兑换处理状态")
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public CommonResult<Boolean> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        return CommonResult.success(userWarrantExchangeService.updateStatus(id, status));
    }
}
