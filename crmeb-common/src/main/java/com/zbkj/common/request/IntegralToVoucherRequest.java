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
 * 积分兑换消费券请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "IntegralToVoucherRequest", description = "积分兑换消费券")
public class IntegralToVoucherRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用于兑换的积分数", required = true)
    @NotNull(message = "积分数不能为空")
    @Min(value = 1, message = "积分数至少为1")
    private Integer integral;
}
