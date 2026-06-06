package com.zbkj.common.response;

import com.zbkj.common.model.system.SystemUserLevel;
import com.zbkj.common.model.system.SystemUserLevelBrokerage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 会员等级详情（含返佣配置）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemUserLevelInfoResponse对象", description = "会员等级详情")
public class SystemUserLevelInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "等级信息")
    private SystemUserLevel level;

    @ApiModelProperty(value = "返佣配置")
    private SystemUserLevelBrokerage brokerage;
}
