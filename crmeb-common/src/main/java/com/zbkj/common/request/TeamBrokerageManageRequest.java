package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 团队奖全局配置
 */
@Data
@ApiModel(value = "TeamBrokerageManageRequest对象", description = "团队奖全局配置")
public class TeamBrokerageManageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "是否启用团队极差奖：1=启用，0=关闭")
    @NotNull(message = "团队奖开关不能为空")
    @Range(min = 0, max = 1, message = "团队奖开关只能为0或1")
    private Integer teamBrokerageStatus;

    @ApiModelProperty(value = "向上追溯层数，0=不限")
    @NotNull(message = "追溯层数不能为空")
    @Min(value = 0, message = "追溯层数不能小于0")
    private Integer teamBrokerageMaxDepth;

    @ApiModelProperty(value = "团队奖到账方式：1-支付订单到账，2-订单完成到账")
    @Range(min = 1, max = 2, message = "团队奖到账方式只能为1或2")
    private Integer teamBrokerageCreditTiming;
}
