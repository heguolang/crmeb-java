package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 提现设置保存请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ExtractConfigRequest", description = "提现设置")
public class ExtractConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "佣金提现开关 0/1")
    private String brokerageExtractSwitch;

    @ApiModelProperty(value = "佣金最低提现金额")
    private String brokerageExtractMinPrice;

    @ApiModelProperty(value = "佣金手续费类型 fixed/percent")
    private String brokerageExtractFeeType;

    @ApiModelProperty(value = "佣金手续费值")
    private String brokerageExtractFee;

    @ApiModelProperty(value = "佣金提现倍数，0不限制")
    private String brokerageExtractMultiple;

    @ApiModelProperty(value = "余额提现开关 0/1")
    private String balanceExtractSwitch;

    @ApiModelProperty(value = "余额最低提现金额")
    private String balanceExtractMinPrice;

    @ApiModelProperty(value = "余额手续费类型 fixed/percent")
    private String balanceExtractFeeType;

    @ApiModelProperty(value = "余额手续费值")
    private String balanceExtractFee;

    @ApiModelProperty(value = "余额提现倍数，0不限制")
    private String balanceExtractMultiple;

    @ApiModelProperty(value = "提现银行卡列表，换行分隔")
    private String userExtractBank;

    @ApiModelProperty(value = "提现支持方式，逗号分隔：bank,weixin,alipay")
    private String userExtractType;
}
