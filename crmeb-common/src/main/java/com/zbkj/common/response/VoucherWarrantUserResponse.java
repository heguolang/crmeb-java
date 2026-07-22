package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户消费券/权证资产响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "VoucherWarrantUserResponse", description = "用户消费券权证资产")
public class VoucherWarrantUserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "积分")
    private BigDecimal integral;

    @ApiModelProperty(value = "消费券")
    private BigDecimal consumeVoucher;

    @ApiModelProperty(value = "权证")
    private BigDecimal warrant;

    @ApiModelProperty(value = "权证第三方地址")
    private String warrantAddress;

    @ApiModelProperty(value = "权证地址最近绑定时间")
    private Date warrantAddressTime;

    @ApiModelProperty(value = "余额")
    private BigDecimal nowMoney;

    @ApiModelProperty(value = "多少积分=1消费券")
    private BigDecimal integralToVoucherRatio;

    @ApiModelProperty(value = "多少消费券=1元余额")
    private BigDecimal voucherToBalanceRatio;

    @ApiModelProperty(value = "兑1权证所需消费券")
    private BigDecimal warrantNeedVoucher;

    @ApiModelProperty(value = "兑1权证所需积分")
    private Integer warrantNeedIntegral;

    @ApiModelProperty(value = "功能是否开启")
    private Boolean switchOn;
}
