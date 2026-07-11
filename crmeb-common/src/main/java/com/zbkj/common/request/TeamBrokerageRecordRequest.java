package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * 团队奖资金记录查询
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TeamBrokerageRecordRequest对象", description = "团队奖资金记录请求对象")
public class TeamBrokerageRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "佣金分级：10=团队极差 11=团队平级，空=全部团队奖")
    @Range(min = 10, max = 11, message = "团队奖类型只能为10或11")
    private Integer brokerageLevel;

    @ApiModelProperty(value = "状态：1创建 2冻结 3完成 4失效，空=全部")
    @Range(min = 1, max = 4, message = "未知的状态")
    private Integer status;

    @ApiModelProperty(value = "关键字：UID/昵称/手机号/订单号")
    private String keywords;

    @ApiModelProperty(value = "时间范围：today/yesterday/lately7/lately30/month/year 或自定义 yyyy-MM-dd,yyyy-MM-dd")
    private String dateLimit;
}
