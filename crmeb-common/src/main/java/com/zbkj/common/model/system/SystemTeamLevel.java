package com.zbkj.common.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 团队等级
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_system_team_level")
@ApiModel(value = "SystemTeamLevel对象", description = "团队等级")
public class SystemTeamLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "团队等级名称")
    private String name;

    @ApiModelProperty(value = "团队等级序号")
    private Integer grade;

    @ApiModelProperty(value = "自购订单金额门槛(元)")
    private BigDecimal selfOrderAmount;

    @ApiModelProperty(value = "团队订单金额门槛(元)")
    private BigDecimal teamOrderAmount;

    @ApiModelProperty(value = "自购订单统计时机：1=支付成功，2=订单完成")
    private Integer selfOrderTriggerType;

    @ApiModelProperty(value = "团队订单统计时机：1=支付成功，2=订单完成")
    private Integer teamOrderTriggerType;

    @ApiModelProperty(value = "等级权益描述")
    private String description;

    @ApiModelProperty(value = "等级图标")
    private String icon;

    @ApiModelProperty(value = "是否显示")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "团队等级配置")
    @TableField(exist = false)
    private SystemTeamLevelConfig config;
}
