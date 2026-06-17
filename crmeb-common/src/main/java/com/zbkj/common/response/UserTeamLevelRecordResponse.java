package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 团队等级变更记录列表响应
 */
@Data
@ApiModel(value = "UserTeamLevelRecordResponse对象", description = "团队等级变更记录")
public class UserTeamLevelRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "团队等级ID")
    private Integer teamLevelId;

    @ApiModelProperty(value = "团队等级名称")
    private String teamLevelName;

    @ApiModelProperty(value = "团队等级序号")
    private Integer grade;

    @ApiModelProperty(value = "0:禁止,1:正常")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}

