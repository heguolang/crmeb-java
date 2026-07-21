package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 兑换权证请求（积分或消费券单独兑换）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ExchangeWarrantRequest", description = "积分或消费券单独兑换权证")
public class ExchangeWarrantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付方式：integral=积分 voucher=消费券", required = true)
    @NotBlank(message = "请选择兑换方式")
    private String payType;

    @ApiModelProperty(value = "用于兑换的数量（积分数或消费券数）", required = true)
    @NotNull(message = "兑换数量不能为空")
    @DecimalMin(value = "0.001", message = "兑换数量必须大于0")
    private BigDecimal amount;

    @ApiModelProperty(value = "权证第三方地址（兑换时一并提交）", required = true)
    @NotBlank(message = "请输入权证地址")
    private String address;
}
