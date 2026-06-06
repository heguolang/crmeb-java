package com.zbkj.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.system.SystemUserLevel;
import com.zbkj.common.model.system.SystemUserLevelBrokerage;
import com.zbkj.common.request.SystemUserLevelBrokerageRequest;
import com.zbkj.common.request.SystemUserLevelBrokerageSaveRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.SystemUserLevelBrokerageService;
import com.zbkj.service.service.SystemUserLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员等级返佣配置 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/system/user/level/brokerage")
@Api(tags = "设置 -- 会员等级返佣")
public class SystemUserLevelBrokerageController {

    @Autowired
    private SystemUserLevelBrokerageService systemUserLevelBrokerageService;

    @Autowired
    private SystemUserLevelService systemUserLevelService;

    /**
     * 返佣配置列表
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:list')")
    @ApiOperation(value = "返佣配置列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<SystemUserLevelBrokerage>> getList() {
        return CommonResult.success(systemUserLevelBrokerageService.getList());
    }

    /**
     * 返佣配置详情
     * @param levelId 等级id
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:list')")
    @ApiOperation(value = "返佣配置详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemUserLevelBrokerage> info(@RequestParam(value = "levelId") Integer levelId) {
        validateLevelExists(levelId);
        return CommonResult.success(systemUserLevelBrokerageService.getByLevelId(levelId));
    }

    /**
     * 保存或更新返佣配置
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:update')")
    @ApiOperation(value = "保存返佣配置")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated SystemUserLevelBrokerageSaveRequest request) {
        validateLevelExists(request.getLevelId());
        SystemUserLevelBrokerageRequest brokerageRequest = new SystemUserLevelBrokerageRequest();
        brokerageRequest.setSelfBrokerageRate(request.getSelfBrokerageRate());
        brokerageRequest.setBrokerageRateOne(request.getBrokerageRateOne());
        brokerageRequest.setBrokerageRateTwo(request.getBrokerageRateTwo());
        if (systemUserLevelBrokerageService.saveOrUpdateByLevelId(request.getLevelId(), brokerageRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除返佣配置
     * @param levelId 等级id
     */
    @PreAuthorize("hasAuthority('admin:system:user:level:delete')")
    @ApiOperation(value = "删除返佣配置")
    @RequestMapping(value = "/delete/{levelId}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "levelId") Integer levelId) {
        validateLevelExists(levelId);
        if (systemUserLevelBrokerageService.deleteByLevelId(levelId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    private void validateLevelExists(Integer levelId) {
        SystemUserLevel level = systemUserLevelService.getById(levelId);
        if (ObjectUtil.isNull(level) || level.getIsDel()) {
            throw new CrmebException("会员等级不存在");
        }
    }
}
