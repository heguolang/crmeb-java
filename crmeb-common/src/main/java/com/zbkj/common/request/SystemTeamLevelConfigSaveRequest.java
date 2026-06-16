package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 团队等级配置保存请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelConfigSaveRequest对象", description = "团队等级配置保存")
public class SystemTeamLevelConfigSaveRequest extends SystemTeamLevelConfigRequest {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队等级ID", required = true)
    @NotNull(message = "团队等级ID不能为空")
    private Integer teamLevelId;

    @ApiModelProperty(value = "团队极差比例(%)", required = true)
    @NotNull(message = "团队极差比例不能为空")
    @Override
    public Integer getTeamBrokerageRate() {
        return super.getTeamBrokerageRate();
    }

    @ApiModelProperty(value = "平级奖比例(%)", required = true)
    @NotNull(message = "平级奖比例不能为空")
    @Override
    public Integer getPeerAwardRate() {
        return super.getPeerAwardRate();
    }
}
