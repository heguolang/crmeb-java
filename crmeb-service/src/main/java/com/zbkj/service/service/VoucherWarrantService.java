package com.zbkj.service.service;

import com.zbkj.common.model.user.UserVoucherRecord;
import com.zbkj.common.model.user.UserWarrantRecord;
import com.zbkj.common.request.*;
import com.zbkj.common.response.VoucherWarrantConfigResponse;
import com.zbkj.common.response.VoucherWarrantUserResponse;

import java.util.List;

/**
 * 消费券与权证兑换服务
 */
public interface VoucherWarrantService {

    VoucherWarrantUserResponse getUserAsset();

    Boolean integralToVoucher(IntegralToVoucherRequest request);

    Boolean voucherToBalance(VoucherToBalanceRequest request);

    Boolean exchangeWarrant(ExchangeWarrantRequest request);

    List<UserVoucherRecord> getVoucherRecordList(PageParamRequest pageParamRequest);

    List<UserWarrantRecord> getWarrantRecordList(PageParamRequest pageParamRequest);

    /**
     * 每日积分强制释放到消费券
     */
    void dailyReleaseIntegralToVoucher();

    VoucherWarrantConfigResponse getConfig();

    Boolean saveConfig(VoucherWarrantConfigRequest request);

    Boolean adminOperate(UserOperateVoucherWarrantRequest request);

    /**
     * 绑定/更新权证第三方地址（不扣减权证）
     */
    Boolean bindWarrantAddress(WarrantAddressRequest request);
}
