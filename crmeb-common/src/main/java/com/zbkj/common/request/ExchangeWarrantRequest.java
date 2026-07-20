package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 兑换权证请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ExchangeWarrantRequest", description = "消费券+积分兑换权证")
public class ExchangeWarrantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "兑换份数", required = true)
    @NotNull(message = "兑换份数不能为空")
    @Min(value = 1, message = "兑换份数至少为1")
    private Integer quantity;
}
