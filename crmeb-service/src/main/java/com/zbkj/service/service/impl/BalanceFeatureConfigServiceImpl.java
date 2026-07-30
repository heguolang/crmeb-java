package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.request.BalanceFeatureConfigRequest;
import com.zbkj.common.response.BalanceFeatureConfigResponse;
import com.zbkj.service.service.BalanceFeatureConfigService;
import com.zbkj.service.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 余额功能设置
 */
@Service
public class BalanceFeatureConfigServiceImpl implements BalanceFeatureConfigService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public BalanceFeatureConfigResponse getConfig() {
        BalanceFeatureConfigResponse response = new BalanceFeatureConfigResponse();
        response.setBalanceTransferSwitch(defaultSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_TRANSFER_SWITCH), "1"));
        response.setBrokerageToYueSwitch(defaultSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BROKERAGE_TO_YUE_SWITCH), "1"));
        response.setBalanceRechargeSwitch(rechargeToSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_RECHARGE_SWITCH)));
        return response;
    }

    @Override
    public Boolean saveConfig(BalanceFeatureConfigRequest request) {
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_TRANSFER_SWITCH, switchVal(request.getBalanceTransferSwitch()));
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BROKERAGE_TO_YUE_SWITCH, switchVal(request.getBrokerageToYueSwitch()));
        // 兼容既有 recharge_switch（true/false）与 APP Boolean.valueOf 解析
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_KEY_RECHARGE_SWITCH,
                "1".equals(switchVal(request.getBalanceRechargeSwitch())) ? "true" : "false");
        return Boolean.TRUE;
    }

    @Override
    public boolean isBalanceTransferOpen() {
        return "1".equals(defaultSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_TRANSFER_SWITCH), "1"));
    }

    @Override
    public boolean isBrokerageToYueOpen() {
        return "1".equals(defaultSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BROKERAGE_TO_YUE_SWITCH), "1"));
    }

    @Override
    public boolean isBalanceRechargeOpen() {
        return "1".equals(rechargeToSwitch(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_KEY_RECHARGE_SWITCH)));
    }

    private String switchVal(String val) {
        return "1".equals(StrUtil.trim(val)) ? "1" : "0";
    }

    private String defaultSwitch(String val, String def) {
        if (StrUtil.isBlank(val)) {
            return def;
        }
        String v = val.trim();
        if ("1".equals(v) || "true".equalsIgnoreCase(v)) {
            return "1";
        }
        if ("0".equals(v) || "false".equalsIgnoreCase(v)) {
            return "0";
        }
        return def;
    }

    /** recharge_switch 历史值为 true/false，统一转成 0/1 给后台开关用 */
    private String rechargeToSwitch(String val) {
        if (StrUtil.isBlank(val)) {
            return "1";
        }
        String v = val.trim();
        if ("1".equals(v) || "true".equalsIgnoreCase(v)) {
            return "1";
        }
        return "0";
    }
}
