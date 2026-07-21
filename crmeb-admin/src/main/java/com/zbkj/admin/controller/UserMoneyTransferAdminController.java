package com.zbkj.admin.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserMoneyTransferSearchRequest;
import com.zbkj.common.response.UserMoneyTransferResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserMoneyTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 余额转账记录
 */
@Slf4j
@RestController
@RequestMapping("api/admin/finance/transfer")
@Api(tags = "财务 -- 余额转账")
public class UserMoneyTransferAdminController {

    @Autowired
    private UserMoneyTransferService userMoneyTransferService;

    @PreAuthorize("hasAuthority('admin:finance:transfer:list')")
    @ApiOperation(value = "余额转账记录")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserMoneyTransferResponse>> getList(
            @Validated UserMoneyTransferSearchRequest request,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(userMoneyTransferService.getAdminList(request, pageParamRequest)));
    }
}
