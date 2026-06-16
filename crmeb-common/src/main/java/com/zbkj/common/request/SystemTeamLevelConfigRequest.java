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
 * 团队等级配置请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelConfigRequest对象", description = "团队等级配置")
public class SystemTeamLevelConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队极差比例(%)", example = "6")
    @Min(value = 0, message = "团队极差比例不能小于0")
    @Max(value = 100, message = "团队极差比例不能大于100")
    private Integer teamBrokerageRate;

    @ApiModelProperty(value = "平级奖比例(%)", example = "1")
    @Min(value = 0, message = "平级奖比例不能小于0")
    @Max(value = 100, message = "平级奖比例不能大于100")
    private Integer peerAwardRate;
}
