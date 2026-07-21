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
 * 用户余额互转请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserMoneyTransferRequest", description = "用户余额互转请求")
public class UserMoneyTransferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "收款用户UID", required = true)
    @NotNull(message = "请输入收款用户ID")
    private Integer toUid;

    @ApiModelProperty(value = "转账金额", required = true)
    @NotNull(message = "请输入转账金额")
    @DecimalMin(value = "0.01", message = "转账金额必须大于0")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String mark;
}
