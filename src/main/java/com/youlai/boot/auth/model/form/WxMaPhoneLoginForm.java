package com.youlai.boot.auth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序手机号快捷登录表单（多租户扩展：支持传入 appId）
 */
@Data
@Schema(description = "微信小程序手机号快捷登录请求")
public class WxMaPhoneLoginForm {

    @NotBlank(message = "微信登录凭证不能为空")
    @Schema(description = "微信登录凭证（wx.login 获取）", example = "0xxx")
    private String loginCode;

    @NotBlank(message = "手机号授权凭证不能为空")
    @Schema(description = "手机号授权凭证（getPhoneNumber 事件获取）", example = "0xxx")
    private String phoneCode;

    @Schema(description = "应用 AppId（用于解析归属租户）")
    private String appId;
}
