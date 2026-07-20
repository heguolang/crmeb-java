package com.zbkj.admin.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AdminIntegralSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserOperateVoucherWarrantRequest;
import com.zbkj.common.request.VoucherWarrantConfigRequest;
import com.zbkj.common.response.UserVoucherRecordResponse;
import com.zbkj.common.response.UserWarrantRecordResponse;
import com.zbkj.common.response.VoucherWarrantConfigResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.UserVoucherRecordService;
import com.zbkj.service.service.UserWarrantRecordService;
import com.zbkj.service.service.VoucherWarrantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消费券与权证 - 管理端
 */
@Slf4j
@RestController
@RequestMapping("api/admin/voucher")
@Api(tags = "消费券权证管理")
public class VoucherWarrantAdminController {

    @Autowired
    private VoucherWarrantService voucherWarrantService;

    @Autowired
    private UserVoucherRecordService userVoucherRecordService;

    @Autowired
    private UserWarrantRecordService userWarrantRecordService;

    @PreAuthorize("hasAuthority('admin:voucher:warrant:config')")
    @ApiOperation(value = "获取配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<VoucherWarrantConfigResponse> getConfig() {
        return CommonResult.success(voucherWarrantService.getConfig());
    }

    @PreAuthorize("hasAuthority('admin:voucher:warrant:config')")
    @ApiOperation(value = "保存配置")
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public CommonResult<Boolean> saveConfig(@RequestBody @Validated VoucherWarrantConfigRequest request) {
        return CommonResult.success(voucherWarrantService.saveConfig(request));
    }

    @PreAuthorize("hasAuthority('admin:user:voucher:list')")
    @ApiOperation(value = "消费券流水")
    @RequestMapping(value = "/record/list", method = RequestMethod.POST)
    public CommonResult<CommonPage<UserVoucherRecordResponse>> voucherList(@RequestBody @Validated AdminIntegralSearchRequest request,
                                                                          @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(userVoucherRecordService.findAdminList(request, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:user:warrant:list')")
    @ApiOperation(value = "权证流水")
    @RequestMapping(value = "/warrant/record/list", method = RequestMethod.POST)
    public CommonResult<CommonPage<UserWarrantRecordResponse>> warrantList(@RequestBody @Validated AdminIntegralSearchRequest request,
                                                                          @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(userWarrantRecordService.findAdminList(request, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:user:operate:founds')")
    @ApiOperation(value = "后台调整消费券/权证")
    @RequestMapping(value = "/operate", method = RequestMethod.POST)
    public CommonResult<Boolean> operate(@RequestBody @Validated UserOperateVoucherWarrantRequest request) {
        return CommonResult.success(voucherWarrantService.adminOperate(request));
    }
}
