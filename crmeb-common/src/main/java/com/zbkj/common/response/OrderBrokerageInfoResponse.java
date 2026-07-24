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
 * 订单分佣明细响应对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "OrderBrokerageInfoResponse", description = "订单分佣明细")
public class OrderBrokerageInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录id")
    private Integer id;

    @ApiModelProperty(value = "获佣用户uid")
    private Integer uid;

    @ApiModelProperty(value = "获佣用户昵称")
    private String userName;

    @ApiModelProperty(value = "获佣用户账号")
    private String account;

    @ApiModelProperty(value = "奖励类型编码")
    private Integer brokerageLevel;

    @ApiModelProperty(value = "奖励类型名称")
    private String brokerageLevelName;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "佣金金额")
    private BigDecimal price;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "状态名称")
    private String statusName;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
