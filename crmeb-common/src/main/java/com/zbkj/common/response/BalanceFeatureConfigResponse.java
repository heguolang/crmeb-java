package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 余额功能设置响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "BalanceFeatureConfigResponse", description = "余额功能设置")
public class BalanceFeatureConfigResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "余额互转开关 0/1")
    private String balanceTransferSwitch;

    @ApiModelProperty(value = "佣金转余额开关 0/1")
    private String brokerageToYueSwitch;

    @ApiModelProperty(value = "余额充值开关 0/1")
    private String balanceRechargeSwitch;
}
