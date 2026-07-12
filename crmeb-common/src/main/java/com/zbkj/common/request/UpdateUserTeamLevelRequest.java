package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新用户团队等级
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UpdateUserTeamLevelRequest", description = "更新用户团队等级")
public class UpdateUserTeamLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户uid", required = true)
    @NotNull(message = "用户id不能为空")
    private Integer uid;

    @ApiModelProperty(value = "团队等级ID，0表示清空", required = true)
    @NotNull(message = "团队等级不能为空")
    private Integer teamLevelId;
}
