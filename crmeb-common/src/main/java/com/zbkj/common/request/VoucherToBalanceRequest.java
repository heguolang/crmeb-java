package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 消费券兑换余额请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "VoucherToBalanceRequest", description = "消费券兑换余额")
public class VoucherToBalanceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用于兑换的消费券数量", required = true)
    @NotNull(message = "消费券数量不能为空")
    @DecimalMin(value = "0.01", message = "消费券数量至少为0.01")
    private BigDecimal voucher;
}
