package com.zbkj.common.model.finance;

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
 * 用户余额互转记录
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_money_transfer")
@ApiModel(value = "UserMoneyTransfer对象", description = "用户余额互转记录")
public class UserMoneyTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "转账单号")
    private String transferNo;

    @ApiModelProperty(value = "转出用户UID")
    private Integer fromUid;

    @ApiModelProperty(value = "转入用户UID")
    private Integer toUid;

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

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
