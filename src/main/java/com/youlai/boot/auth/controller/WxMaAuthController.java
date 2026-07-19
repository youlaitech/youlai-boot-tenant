package com.youlai.boot.auth.controller;

import com.youlai.boot.auth.model.form.WxMaBindMobileForm;
import com.youlai.boot.auth.model.form.WxMaPhoneLoginForm;
import com.youlai.boot.auth.model.vo.WxMaLoginVO;
import com.youlai.boot.auth.service.WxMaAuthService;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.annotation.RateLimit;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 微信小程序认证控制层（多租户扩展：登录接口支持传入 appId）
 */
@Tag(name = "13.微信小程序认证")
@RestController
@RequestMapping("/api/v1/wxma/auth")
@RequiredArgsConstructor
@Slf4j
public class WxMaAuthController {

    private final WxMaAuthService wxMaAuthService;

    @Operation(summary = "静默登录")
    @PostMapping("/silent-login")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    @RateLimit
    public Result<WxMaLoginVO> silentLogin(
            @Parameter(description = "微信登录凭证（wx.login 获取）", required = true, example = "0xxx")
            @RequestParam String code,
            @Parameter(description = "应用 AppId（用于解析归属租户）", example = "wx123")
            @RequestParam(required = false) String appId
    ) {
        return Result.success(wxMaAuthService.silentLogin(code, appId));
    }

    @Operation(summary = "手机号快捷登录")
    @PostMapping("/phone-login")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    @RateLimit
    public Result<AuthenticationToken> phoneLogin(@Valid @RequestBody WxMaPhoneLoginForm req) {
        return Result.success(wxMaAuthService.phoneLogin(req.getLoginCode(), req.getPhoneCode(), req.getAppId()));
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind-mobile")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    @RateLimit
    public Result<AuthenticationToken> bindMobile(@Valid @RequestBody WxMaBindMobileForm req) {
        return Result.success(wxMaAuthService.bindMobile(req.getOpenid(), req.getMobile(), req.getSmsCode(), req.getAppId()));
    }
}