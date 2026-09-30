package com.zbkj.common.request;

import com.zbkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="FundsMonitorRequest对象", description="资金监控")
public class FundsMonitorRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    @ApiModelProperty(value = "添加时间")
    private String dateLimit;

    @ApiModelProperty(value = "明细类型:recharge-充值支付，admin-后台操作，productRefund-商品退款，payProduct-购买商品，order-订单佣金，orderDistribution-分销佣金，orderTeamGap-团队级差奖，orderTeamPeer-团队平级奖，withdraw-佣金提现")
    @StringContains(limitValues = {"recharge", "admin", "productRefund", "payProduct",
            "order", "orderDistribution", "orderTeamGap", "orderTeamPeer", "withdraw"}, message = "请选择正确的明细类型")
    private String title;

    @ApiModelProperty(value = "账户类型:all-全部（默认），now_money-余额，integral-信用值，brokerage_price-佣金")
    private String category;

    @ApiModelProperty(value = "关联单号（订单号等，模糊匹配）")
    private String linkId;

}
