package com.zbkj.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zbkj.common.constants.RegularConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 手机号注册请求对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RegisterRequest对象", description = "手机号注册")
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = RegularConstants.PHONE_TWO, message = "手机号码格式错误")
    @JsonProperty(value = "account")
    private String phone;

    @ApiModelProperty(value = "验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = RegularConstants.VALIDATE_CODE_NUM_SIX, message = "验证码格式错误，验证码必须为6位数字")
    private String captcha;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$", message = "密码须为6-16位字母数字组合")
    private String password;

    @ApiModelProperty(value = "推广人id")
    @JsonProperty(value = "spread_spid")
    private Integer spreadPid = 0;
}
