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
 * 团队奖漏发检测结果行
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TeamBrokerageAuditItemResponse对象", description = "团队奖漏发检测结果行")
public class TeamBrokerageAuditItemResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单主键id")
    private Integer orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "买家uid")
    private Integer uid;

    @ApiModelProperty(value = "买家昵称")
    private String userName;

    @ApiModelProperty(value = "实付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "支付时间")
    private Date payTime;

    @ApiModelProperty(value = "漏发条数")
    private Integer missingCount;

    @ApiModelProperty(value = "漏发合计金额")
    private BigDecimal missingAmount;

    @ApiModelProperty(value = "漏发明细")
    private String missingDetail;
}
