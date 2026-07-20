package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 消费券记录响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserVoucherRecordResponse", description = "消费券记录响应")
public class UserVoucherRecordResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录id")
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "变动消费券")
    private BigDecimal voucher;

    @ApiModelProperty(value = "剩余")
    private BigDecimal balance;

    @ApiModelProperty(value = "类型：1增加2扣减")
    private Integer type;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
