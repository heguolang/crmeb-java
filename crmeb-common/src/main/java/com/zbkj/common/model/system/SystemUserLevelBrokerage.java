package com.zbkj.common.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员等级返佣配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_system_user_level_brokerage")
@ApiModel(value = "SystemUserLevelBrokerage对象", description = "会员等级返佣配置")
public class SystemUserLevelBrokerage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会员等级ID")
    private Integer levelId;

    @ApiModelProperty(value = "自购返佣比例(%)")
    private Integer selfBrokerageRate;

    @ApiModelProperty(value = "一级返佣比例(%)")
    private Integer brokerageRateOne;

    @ApiModelProperty(value = "二级返佣比例(%)")
    private Integer brokerageRateTwo;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
