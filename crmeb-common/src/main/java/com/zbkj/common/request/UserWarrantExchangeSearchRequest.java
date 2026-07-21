package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 权证兑换申请搜索
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserWarrantExchangeSearchRequest", description = "权证兑换申请搜索")
public class UserWarrantExchangeSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户UID")
    private Integer uid;

    @ApiModelProperty(value = "状态：0=待处理 1=已处理，空=全部")
    private Integer status;

    @ApiModelProperty(value = "支付方式：integral/voucher")
    private String payType;

    @ApiModelProperty(value = "关键字：昵称/地址")
    private String keywords;

    @ApiModelProperty(value = "时间范围")
    private String dateLimit;
}
