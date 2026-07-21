package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 转账收款人校验响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserMoneyTransferCheckResponse", description = "转账收款人校验")
public class UserMoneyTransferCheckResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "收款用户UID")
    private Integer uid;

    @ApiModelProperty(value = "收款用户昵称（脱敏）")
    private String nickname;
}
