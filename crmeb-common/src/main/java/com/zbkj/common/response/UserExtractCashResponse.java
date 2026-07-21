package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提现用户信息响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserExtractCashResponse对象", description="提现用户信息响应对象")
public class UserExtractCashResponse implements Serializable {
    public UserExtractCashResponse(){}
    public UserExtractCashResponse(String minPrice, BigDecimal commissionCount, BigDecimal brokenCommission, String brokenDay) {
        this.minPrice = minPrice;
        this.commissionCount = commissionCount;
        this.brokenCommission = brokenCommission;
        this.brokenDay = brokenDay;
    }

    public UserExtractCashResponse(String minPrice, BigDecimal commissionCount, BigDecimal brokenCommission, String brokenDay, String extractFee) {
        this.minPrice = minPrice;
        this.commissionCount = commissionCount;
        this.brokenCommission = brokenCommission;
        this.brokenDay = brokenDay;
        this.extractFee = extractFee;
    }

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "提现来源 brokerage/balance")
    private String extractSource;

    @ApiModelProperty(value = "提现开关 0关闭 1开启")
    private String extractSwitch;

    @ApiModelProperty(value = "提现最低金额")
    private String minPrice;

    @ApiModelProperty(value = "手续费类型 fixed/percent")
    private String extractFeeType;

    @ApiModelProperty(value = "手续费配置值（固定元或百分比）")
    private String extractFee;

    @ApiModelProperty(value = "提现倍数，0表示不限制")
    private String extractMultiple;

    @ApiModelProperty(value = "可提现金额（佣金或余额）")
    private BigDecimal commissionCount;

    @ApiModelProperty(value = "冻结佣金（仅佣金提现有意义）")
    private BigDecimal brokenCommission;

    @ApiModelProperty(value = "冻结天数（仅佣金提现有意义）")
    private String brokenDay;
}
