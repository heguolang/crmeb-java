package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 会员等级返佣配置请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemUserLevelBrokerageRequest对象", description = "会员等级返佣配置")
public class SystemUserLevelBrokerageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自购返佣比例(%)", example = "3")
    @Min(value = 0, message = "自购返佣比例不能小于0")
    @Max(value = 100, message = "自购返佣比例不能大于100")
    private Integer selfBrokerageRate;

    @ApiModelProperty(value = "一级返佣比例(%)", example = "8")
    @Min(value = 0, message = "一级返佣比例不能小于0")
    @Max(value = 100, message = "一级返佣比例不能大于100")
    private Integer brokerageRateOne;

    @ApiModelProperty(value = "二级返佣比例(%)", example = "4")
    @Min(value = 0, message = "二级返佣比例不能小于0")
    @Max(value = 100, message = "二级返佣比例不能大于100")
    private Integer brokerageRateTwo;
}
