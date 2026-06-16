package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 团队等级请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelRequest对象", description = "团队等级")
public class SystemTeamLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队等级id")
    private Integer id;

    @ApiModelProperty(value = "团队等级名称")
    @NotBlank(message = "团队等级名称不能为空")
    @Length(max = 50, message = "团队等级名称不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "团队等级序号")
    @NotNull(message = "团队等级序号不能为空")
    @Min(value = 1, message = "团队等级序号最小为1")
    private Integer grade;

    @ApiModelProperty(value = "自购订单金额门槛(元)", example = "1000.00")
    @NotNull(message = "自购订单金额不能为空")
    @DecimalMin(value = "0", message = "自购订单金额不能小于0")
    private BigDecimal selfOrderAmount;

    @ApiModelProperty(value = "团队订单金额门槛(元)", example = "5000.00")
    @NotNull(message = "团队订单金额不能为空")
    @DecimalMin(value = "0", message = "团队订单金额不能小于0")
    private BigDecimal teamOrderAmount;

    @ApiModelProperty(value = "自购订单统计时机：1=支付成功，2=订单完成", example = "2")
    private Integer selfOrderTriggerType;

    @ApiModelProperty(value = "团队订单统计时机：1=支付成功，2=订单完成", example = "2")
    private Integer teamOrderTriggerType;

    @ApiModelProperty(value = "等级权益描述")
    @Length(max = 500, message = "权益描述不能超过500个字符")
    private String description;

    @ApiModelProperty(value = "等级图标")
    @NotBlank(message = "等级图标不能为空")
    private String icon;

    @ApiModelProperty(value = "是否显示")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;

    @ApiModelProperty(value = "团队等级配置")
    @Valid
    private SystemTeamLevelConfigRequest config;
}
