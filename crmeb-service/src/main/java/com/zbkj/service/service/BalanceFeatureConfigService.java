package com.zbkj.service.service;

import com.zbkj.common.request.BalanceFeatureConfigRequest;
import com.zbkj.common.response.BalanceFeatureConfigResponse;

/**
 * 余额功能设置
 */
public interface BalanceFeatureConfigService {

    BalanceFeatureConfigResponse getConfig();

    Boolean saveConfig(BalanceFeatureConfigRequest request);

    /** 余额互转是否开启 */
    boolean isBalanceTransferOpen();

    /** 佣金转余额是否开启 */
    boolean isBrokerageToYueOpen();

    /** 余额充值是否开启 */
    boolean isBalanceRechargeOpen();
}
