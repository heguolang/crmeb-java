package com.zbkj.common.response;

import com.zbkj.common.model.system.SystemTeamLevel;
import com.zbkj.common.model.system.SystemTeamLevelConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 团队等级详情
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelInfoResponse对象", description = "团队等级详情")
public class SystemTeamLevelInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队等级信息")
    private SystemTeamLevel level;

    @ApiModelProperty(value = "团队等级配置")
    private SystemTeamLevelConfig config;
}
