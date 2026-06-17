package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 团队关联用户列表响应
 */
@Data
@ApiModel(value = "UserTeamLevelUserResponse对象", description = "团队关联用户")
public class UserTeamLevelUserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "团队等级ID")
    private Integer teamLevelId;

    @ApiModelProperty(value = "团队等级名称")
    private String teamLevelName;

    @ApiModelProperty(value = "团队等级序号")
    private Integer grade;

    @ApiModelProperty(value = "自购已支付累计金额(元)")
    private BigDecimal selfPaidAmount;

    @ApiModelProperty(value = "自购已完成累计金额(元)")
    private BigDecimal selfCompleteAmount;

    @ApiModelProperty(value = "团队已支付累计金额(元)")
    private BigDecimal teamPaidAmount;

    @ApiModelProperty(value = "团队已完成累计金额(元)")
    private BigDecimal teamCompleteAmount;

    @ApiModelProperty(value = "统计更新时间")
    private Date updateTime;
}

