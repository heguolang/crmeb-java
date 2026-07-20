package com.zbkj.front.controller;

import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.ExchangeWarrantRequest;
import com.zbkj.common.request.IntegralToVoucherRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.VoucherToBalanceRequest;
import com.zbkj.common.request.WarrantAddressRequest;
import com.zbkj.common.response.VoucherWarrantUserResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.VoucherWarrantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消费券与权证 - 用户端
 */
@Slf4j
@RestController
@RequestMapping("api/front/voucher")
@Api(tags = "消费券权证")
public class VoucherWarrantController {

    @Autowired
    private VoucherWarrantService voucherWarrantService;

    @ApiOperation(value = "资产概览")
    @RequestMapping(value = "/asset", method = RequestMethod.GET)
    public CommonResult<VoucherWarrantUserResponse> asset() {
        return CommonResult.success(voucherWarrantService.getUserAsset());
    }

    @ApiOperation(value = "积分兑换消费券")
    @RequestMapping(value = "/integral/to/voucher", method = RequestMethod.POST)
    public CommonResult<Boolean> integralToVoucher(@RequestBody @Validated IntegralToVoucherRequest request) {
        return CommonResult.success(voucherWarrantService.integralToVoucher(request));
    }

    @ApiOperation(value = "消费券兑换余额")
    @RequestMapping(value = "/to/balance", method = RequestMethod.POST)
    public CommonResult<Boolean> voucherToBalance(@RequestBody @Validated VoucherToBalanceRequest request) {
        return CommonResult.success(voucherWarrantService.voucherToBalance(request));
    }

    @ApiOperation(value = "消费券+积分兑换权证")
    @RequestMapping(value = "/to/warrant", method = RequestMethod.POST)
    public CommonResult<Boolean> exchangeWarrant(@RequestBody @Validated ExchangeWarrantRequest request) {
        return CommonResult.success(voucherWarrantService.exchangeWarrant(request));
    }

    @ApiOperation(value = "消费券流水")
    @RequestMapping(value = "/record", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserVoucherRecord>> voucherRecord(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(voucherWarrantService.getVoucherRecordList(pageParamRequest)));
    }

    @ApiOperation(value = "权证流水")
    @RequestMapping(value = "/warrant/record", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserWarrantRecord>> warrantRecord(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(voucherWarrantService.getWarrantRecordList(pageParamRequest)));
    }

    @ApiOperation(value = "绑定权证第三方地址")
    @RequestMapping(value = "/warrant/address", method = RequestMethod.POST)
    public CommonResult<Boolean> bindWarrantAddress(@RequestBody @Validated WarrantAddressRequest request) {
        return CommonResult.success(voucherWarrantService.bindWarrantAddress(request));
    }
}
