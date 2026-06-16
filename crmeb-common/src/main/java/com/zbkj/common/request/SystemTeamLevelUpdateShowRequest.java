package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 团队等级显示状态更新请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelUpdateShowRequest对象", description = "团队等级显示状态")
public class SystemTeamLevelUpdateShowRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队等级id")
    @NotNull(message = "团队等级id不能为空")
    private Integer id;

    @ApiModelProperty(value = "是否显示")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;
}
