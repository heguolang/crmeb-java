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
 * 余额转账记录响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserMoneyTransferResponse", description = "余额转账记录")
public class UserMoneyTransferResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "转账单号")
    private String transferNo;

    @ApiModelProperty(value = "转出用户UID")
    private Integer fromUid;

    @ApiModelProperty(value = "转出用户昵称")
    private String fromNickname;

    @ApiModelProperty(value = "转入用户UID")
    private Integer toUid;

    @ApiModelProperty(value = "转入用户昵称")
    private String toNickname;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "转出后余额")
    private BigDecimal fromBalance;

    @ApiModelProperty(value = "转入后余额")
    private BigDecimal toBalance;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "状态：1=成功")
    private Integer status;

    @ApiModelProperty(value = "转账时间")
    private Date createTime;
}
