package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 用户团队等级统计表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_team_level_stat")
@ApiModel(value = "UserTeamLevelStat对象", description = "用户团队等级统计表")
public class UserTeamLevelStat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "自购已支付累计金额(元)")
    private BigDecimal selfPaidAmount;

    @ApiModelProperty(value = "自购已完成累计金额(元)")
    private BigDecimal selfCompleteAmount;

    @ApiModelProperty(value = "团队已支付累计金额(元)")
    private BigDecimal teamPaidAmount;

    @ApiModelProperty(value = "团队已完成累计金额(元)")
    private BigDecimal teamCompleteAmount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}

