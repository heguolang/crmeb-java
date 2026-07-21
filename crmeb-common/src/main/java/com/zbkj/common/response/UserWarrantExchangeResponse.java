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
 * 权证兑换申请响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserWarrantExchangeResponse", description = "权证兑换申请")
public class UserWarrantExchangeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    private Integer id;

    @ApiModelProperty(value = "用户UID")
    private Integer uid;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "支付方式：integral/voucher")
    private String payType;

    @ApiModelProperty(value = "实际消耗积分或消费券")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "兑换权证数量")
    private BigDecimal warrantAmount;

    @ApiModelProperty(value = "权证第三方地址")
    private String address;

    @ApiModelProperty(value = "状态：0=待处理 1=已处理")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "申请时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
