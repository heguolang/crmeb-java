package com.zbkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.constants.SysConfigConstants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.request.ExtractConfigRequest;
import com.zbkj.common.response.ExtractConfigResponse;
import com.zbkj.common.utils.ExtractFeeUtil;
import com.zbkj.service.service.ExtractConfigService;
import com.zbkj.service.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 提现设置
 */
@Service
public class ExtractConfigServiceImpl implements ExtractConfigService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public ExtractConfigResponse getConfig() {
        ExtractConfigResponse response = new ExtractConfigResponse();
        response.setBrokerageExtractSwitch(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BROKERAGE_EXTRACT_SWITCH), "1"));
        response.setBrokerageExtractMinPrice(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE), "0"));
        response.setBrokerageExtractFeeType(ExtractFeeUtil.normalizeFeeType(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE)));
        response.setBrokerageExtractFee(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_EXTRACT_FEE), "0"));
        response.setBrokerageExtractMultiple(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_EXTRACT_MULTIPLE), "0"));
        response.setBalanceExtractSwitch(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_EXTRACT_SWITCH), "0"));
        response.setBalanceExtractMinPrice(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_EXTRACT_MIN_PRICE), "1"));
        response.setBalanceExtractFeeType(ExtractFeeUtil.normalizeFeeType(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE_TYPE)));
        response.setBalanceExtractFee(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE), "0"));
        response.setBalanceExtractMultiple(defaultStr(systemConfigService.getValueByKey(SysConfigConstants.CONFIG_BALANCE_EXTRACT_MULTIPLE), "0"));
        String bank = systemConfigService.getValueByKey(Constants.CONFIG_BANK_LIST);
        response.setUserExtractBank(StrUtil.isBlank(bank) ? "" : bank.replace("\\n", "\n"));
        return response;
    }

    @Override
    public Boolean saveConfig(ExtractConfigRequest request) {
        String broFeeType = ExtractFeeUtil.normalizeFeeType(request.getBrokerageExtractFeeType());
        String balFeeType = ExtractFeeUtil.normalizeFeeType(request.getBalanceExtractFeeType());
        validateMinPrice(request.getBrokerageExtractMinPrice(), "佣金最低提现金额");
        validateFee(broFeeType, request.getBrokerageExtractFee(), "佣金手续费");
        validateMultiple(request.getBrokerageExtractMultiple(), "佣金提现倍数");
        validateMinPrice(request.getBalanceExtractMinPrice(), "余额最低提现金额");
        validateFee(balFeeType, request.getBalanceExtractFee(), "余额手续费");
        validateMultiple(request.getBalanceExtractMultiple(), "余额提现倍数");

        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BROKERAGE_EXTRACT_SWITCH, switchVal(request.getBrokerageExtractSwitch()));
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_EXTRACT_MIN_PRICE, request.getBrokerageExtractMinPrice().trim());
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_EXTRACT_FEE_TYPE, broFeeType);
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_EXTRACT_FEE, request.getBrokerageExtractFee().trim());
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_EXTRACT_MULTIPLE, request.getBrokerageExtractMultiple().trim());
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_EXTRACT_SWITCH, switchVal(request.getBalanceExtractSwitch()));
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_EXTRACT_MIN_PRICE, request.getBalanceExtractMinPrice().trim());
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE_TYPE, balFeeType);
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_EXTRACT_FEE, request.getBalanceExtractFee().trim());
        systemConfigService.updateOrSaveValueByName(SysConfigConstants.CONFIG_BALANCE_EXTRACT_MULTIPLE, request.getBalanceExtractMultiple().trim());
        if (request.getUserExtractBank() != null) {
            systemConfigService.updateOrSaveValueByName(Constants.CONFIG_BANK_LIST, request.getUserExtractBank());
        }
        return Boolean.TRUE;
    }

    private void validateMinPrice(String value, String label) {
        if (StrUtil.isBlank(value)) {
            throw new CrmebException(label + "不能为空");
        }
        BigDecimal v = ExtractFeeUtil.parseNonNegative(value);
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new CrmebException(label + "不能小于0");
        }
    }

    private void validateMultiple(String value, String label) {
        if (StrUtil.isBlank(value)) {
            throw new CrmebException(label + "不能为空");
        }
        BigDecimal v = ExtractFeeUtil.parseNonNegative(value);
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            throw new CrmebException(label + "不能小于0");
        }
    }

    private void validateFee(String feeType, String feeValue, String label) {
        if (StrUtil.isBlank(feeValue)) {
            throw new CrmebException(label + "不能为空");
        }
        BigDecimal v = ExtractFeeUtil.parseNonNegative(feeValue);
        if (SysConfigConstants.EXTRACT_FEE_TYPE_PERCENT.equals(feeType) && v.compareTo(new BigDecimal("100")) > 0) {
            throw new CrmebException(label + "比例不能大于100");
        }
    }

    private String switchVal(String val) {
        return "1".equals(StrUtil.trim(val)) ? "1" : "0";
    }

    private String defaultStr(String val, String def) {
        return StrUtil.isBlank(val) ? def : val.trim();
    }
}
