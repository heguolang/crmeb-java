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
 * 权证兑换申请
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_warrant_exchange")
@ApiModel(value = "UserWarrantExchange对象", description = "权证兑换申请")
public class UserWarrantExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户UID")
    private Integer uid;

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
