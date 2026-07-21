package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 余额转账记录搜索
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserMoneyTransferSearchRequest", description = "余额转账记录搜索")
public class UserMoneyTransferSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "转出用户UID")
    private Integer fromUid;

    @ApiModelProperty(value = "转入用户UID")
    private Integer toUid;

    @ApiModelProperty(value = "用户UID（转出或转入任一侧）")
    private Integer uid;

    @ApiModelProperty(value = "转账单号")
    private String transferNo;

    @ApiModelProperty(value = "时间：today,yesterday,lately7,lately30,month,year,/yyyy-MM-dd/yyyy-MM-dd/")
    private String dateLimit;
}
