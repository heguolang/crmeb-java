package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * 资金监控
 * +----------------------------------------------------------------------
 * | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 * +----------------------------------------------------------------------
 * | Author: CRMEB Team <admin@crmeb.com>
 * +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="BrokerageRecordRequest对象", description="佣金记录请求对象")
public class BrokerageRecordRequest implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    @ApiModelProperty(value = "类型：1-订单返佣，2-申请提现，3-提现失败，4-提现成功，5-佣金转余额")
    @Range(min = 1, max = 5, message = "未知的类型")
    private Integer type;

    @ApiModelProperty(value = "佣金分级筛选（订单返佣时有效）：0自购 1一级 2二级 10团队极差 11团队平级")
    private Integer brokerageLevel;

    @ApiModelProperty(value = "状态：1待入账 2冻结中 3已完成 4已失效 5提现申请，空=全部")
    private Integer status;

}
