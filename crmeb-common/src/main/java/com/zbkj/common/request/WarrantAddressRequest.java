package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 权证第三方地址绑定请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "WarrantAddressRequest", description = "权证第三方地址绑定")
public class WarrantAddressRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "第三方地址", required = true)
    @NotBlank(message = "地址不能为空")
    @Length(max = 255, message = "地址长度不能超过255")
    private String address;
}
