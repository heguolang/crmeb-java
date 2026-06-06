package com.zbkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 设置用户等级表
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
@TableName("eb_system_user_level")
@ApiModel(value="SystemUserLevelRequest对象", description="设置用户等级表")
public class SystemUserLevelRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "等级id")
    private Integer id;

    @ApiModelProperty(value = "等级名称")
    @NotBlank(message = "等级名称不能为空")
    @Length(max = 50, message = "等级名称不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "升级条件类型：1=累计消费金额，2=累计订单数，3=累计消费金额+累计订单数", required = true, example = "1")
    private Integer upgradeType;

    @ApiModelProperty(value = "消费金额统计时机：1=已付款，2=交易完成（upgradeType=1或3时有效）", example = "1")
    private Integer consumptionTriggerType;

    @ApiModelProperty(value = "订单数统计时机：1=已付款，2=交易完成（upgradeType=2或3时有效）", example = "1")
    private Integer orderCountTriggerType;

    @ApiModelProperty(value = "累计消费金额升级门槛（upgradeType=1或3时填写，单位：元）", example = "126")
    @Min(value = 0, message = "消费金额升级门槛不能小于0")
    private Integer experience;

    @ApiModelProperty(value = "累计订单数升级门槛（upgradeType=2或3时填写）", example = "5")
    @Min(value = 0, message = "订单数升级门槛不能小于0")
    private Integer upgradeValue;

    @ApiModelProperty(value = "等级赠送积分（每单固定赠送，手输多少送多少）", example = "200")
    @Min(value = 0, message = "等级赠送积分不能小于0")
    private Integer giveIntegral;

    @ApiModelProperty(value = "等级权益描述", example = "消费满126元升级，每单赠送200积分")
    @Length(max = 500, message = "权益描述不能超过500个字符")
    private String description;

    @ApiModelProperty(value = "会员等级")
    @NotNull(message = "会员等级不能为空")
    @Min(value = 1, message = "会员等级最小为1")
    private Integer grade;

    @ApiModelProperty(value = "享受折扣")
    @NotNull(message = "折扣不能为空")
    @Min(value = 1, message = "折扣值不能小于1")
    @Max(value = 100, message = "折扣值不能大于100")
    private Integer discount;

    @ApiModelProperty(value = "会员图标")
    @NotBlank(message = "会员图标不能为空")
    private String icon;

    @ApiModelProperty(value = "是否显示 1=显示,0=隐藏")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;

    @ApiModelProperty(value = "等级返佣配置")
    @Valid
    private SystemUserLevelBrokerageRequest brokerage;

}
