package com.zbkj.front.controller;

import com.zbkj.common.request.UserMoneyTransferRequest;
import com.zbkj.common.response.UserMoneyTransferCheckResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserMoneyTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户余额互转
 */
@Slf4j
@RestController
@RequestMapping("api/front/user/money")
@Api(tags = "用户 -- 余额转账")
public class UserMoneyTransferController {

    @Autowired
    private UserMoneyTransferService userMoneyTransferService;

    @ApiOperation(value = "校验收款用户")
    @RequestMapping(value = "/transfer/check", method = RequestMethod.GET)
    public CommonResult<UserMoneyTransferCheckResponse> check(@RequestParam(name = "toUid") Integer toUid) {
        return CommonResult.success(userMoneyTransferService.checkReceiver(toUid));
    }

    @ApiOperation(value = "余额转账给指定用户")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public CommonResult<Boolean> transfer(@RequestBody @Validated UserMoneyTransferRequest request) {
        return CommonResult.success(userMoneyTransferService.transfer(request));
    }
}
