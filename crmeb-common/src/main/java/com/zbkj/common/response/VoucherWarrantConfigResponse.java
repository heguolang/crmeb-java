package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 消费券权证配置响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "VoucherWarrantConfigResponse", description = "消费券权证配置")
public class VoucherWarrantConfigResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "多少积分=1消费券（主动兑换）")
    private String integralToVoucherRatio;

    @ApiModelProperty(value = "每日强制释放当前积分的百分比")
    private String integralDailyReleaseRatio;

    @ApiModelProperty(value = "每日释放：多少积分=1消费券")
    private String integralDailyReleaseExchangeRatio;

    @ApiModelProperty(value = "多少消费券=1元余额")
    private String voucherToBalanceRatio;

    @ApiModelProperty(value = "兑1权证所需消费券")
    private String warrantNeedVoucher;

    @ApiModelProperty(value = "兑1权证所需积分")
    private String warrantNeedIntegral;

    @ApiModelProperty(value = "功能开关 0关闭 1开启")
    private String voucherWarrantSwitch;
}
