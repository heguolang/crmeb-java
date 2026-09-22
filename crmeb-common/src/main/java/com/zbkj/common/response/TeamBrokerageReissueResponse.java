package com.zbkj.common.response;

import com.zbkj.common.model.user.UserBrokerageRecord;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 团队奖漏发补发结果
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TeamBrokerageReissueResponse对象", description = "团队奖漏发补发结果")
public class TeamBrokerageReissueResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "本次实际补发的佣金记录")
    private List<UserBrokerageRecord> records;

    @ApiModelProperty(value = "本次补发合计金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "该订单已有团队奖记录数（去重命中）")
    private Integer existingCount;

    @ApiModelProperty(value = "已有记录中处于失效状态的条数")
    private Integer invalidCount;
}
