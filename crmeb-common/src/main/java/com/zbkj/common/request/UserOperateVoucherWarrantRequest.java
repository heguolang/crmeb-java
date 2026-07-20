package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 后台操作消费券/权证
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserOperateVoucherWarrantRequest", description = "后台操作消费券权证")
public class UserOperateVoucherWarrantRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "uid")
    @NotNull
    @Min(value = 1, message = "请输入正确的uid")
    private Integer uid;

    @ApiModelProperty(value = "消费券类型，1=增加，2=减少")
    @NotNull
    @Range(min = 1, max = 2, message = "请选择正确的消费券类型")
    private Integer voucherType;

    @ApiModelProperty(value = "消费券变动值")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99999999.99")
    private BigDecimal voucherValue;

    @ApiModelProperty(value = "权证类型，1=增加，2=减少")
    @NotNull
    @Range(min = 1, max = 2, message = "请选择正确的权证类型")
    private Integer warrantType;

    @ApiModelProperty(value = "权证变动值")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "99999999.99")
    private BigDecimal warrantValue;
}
