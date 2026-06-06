package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 会员等级返佣配置保存请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "SystemUserLevelBrokerageSaveRequest对象", description = "会员等级返佣配置保存")
public class SystemUserLevelBrokerageSaveRequest extends SystemUserLevelBrokerageRequest {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会员等级ID", required = true)
    @NotNull(message = "会员等级ID不能为空")
    private Integer levelId;
}
