package com.youlai.boot.auth.controller;

import com.youlai.boot.framework.captcha.model.CaptchaInfo;
import com.youlai.boot.auth.model.LoginReq;
import com.youlai.boot.common.enums.ActionTypeEnum;
import com.youlai.boot.common.enums.LogModuleEnum;
import com.youlai.boot.common.enums.StatusEnum;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.auth.service.AuthService;
import com.youlai.boot.common.annotation.Log;
import com.youlai.boot.common.result.ResultCode;
import com.youlai.boot.framework.security.model.SysUserDetails;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.system.model.entity.User;
import com.youlai.boot.system.service.TenantService;
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.framework.security.model.UserAuthInfo;
import com.youlai.boot.framework.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;

/**
 * 认证控制层
 *
 * @author Ray.Hao
 * @since 2022/10/16
 */
@Tag(name = "01.认证中心")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaInfo> getCaptcha() {
        CaptchaInfo captcha = authService.getCaptcha();
        return Result.success(captcha);
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    public Result<?> login(HttpServletRequest request, @RequestBody @Valid LoginReq loginReq) {
        String username = loginReq.getUsername();
        String password = loginReq.getPassword();
        Long tenantId = loginReq.getTenantId();

        if (tenantId != null) {
            AuthenticationToken authenticationToken = authService.login(username, password, tenantId);
            return Result.success(authenticationToken);
        }

        // 未指定租户ID，优先从域名解析租户
        String domain = request.getServerName();
        if (domain != null) {
            Long tenantIdFromDomain = tenantService.getTenantIdByDomain(domain);
            if (tenantIdFromDomain != null) {
                AuthenticationToken authenticationToken = authService.login(username, password, tenantIdFromDomain);
                return Result.success(authenticationToken);
            }
        }

        // 查询该用户名在所有租户下的账户
        TenantContextHolder.setIgnoreTenant(true);
        List<User> users;
        try {
            users = userService.listUsersByUsernameAcrossAllTenants(username);
        } finally {
            TenantContextHolder.clear();
        }

        if (users.isEmpty()) {
            return Result.failed("账号或密码错误");
        }

        List<User> activeUsers = users.stream()
                .filter(user -> user.getStatus() != null && StatusEnum.ENABLE.getValue().equals(user.getStatus()))
                .toList();

        if (activeUsers.isEmpty()) {
            return Result.failed("账号或密码错误");
        }

        // 密码校验通过后，才允许进入"选择租户"分支
        List<User> passwordMatchedUsers = activeUsers.stream()
                .filter(user -> Objects.nonNull(user.getPassword()) && passwordEncoder.matches(password, user.getPassword()))
                .toList();

        if (passwordMatchedUsers.isEmpty()) {
            return Result.failed("账号或密码错误");
        }

        // 有租户切换权限的账号，登录后可切换租户
        for (User candidate : passwordMatchedUsers) {
            TenantContextHolder.setTenantId(candidate.getTenantId());
            UserAuthInfo authInfo;
            try {
                authInfo = userService.getAuthInfoByUsernameInTenant(username, candidate.getTenantId());
            } finally {
                TenantContextHolder.clear();
            }
            if (authInfo != null && Boolean.TRUE.equals(authInfo.getCanSwitchTenant())) {
                AuthenticationToken authenticationToken = authService.login(username, password, candidate.getTenantId());
                return Result.success(authenticationToken);
            }
        }

        // 无租户切换权限：仅允许唯一租户账号登录
        if (passwordMatchedUsers.size() == 1) {
            User user = passwordMatchedUsers.get(0);
            AuthenticationToken authenticationToken = authService.login(username, password, user.getTenantId());
            return Result.success(authenticationToken);
        }

        return Result.failed("账号归属多个租户，请使用租户域名或指定租户登录");
    }

    @Operation(summary = "切换租户")
    @PostMapping("/switch-tenant")
    public Result<AuthenticationToken> switchTenant(@RequestParam Long tenantId) {
        if (!tenantService.hasTenantSwitchPermission()) {
            return Result.failed("无权限");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SysUserDetails details)) {
            return Result.failed(ResultCode.ACCESS_TOKEN_INVALID);
        }

        boolean canAccess = tenantService.canAccessTenant(details.getUserId(), tenantId);
        if (!canAccess) {
            return Result.failed("无权限");
        }

        SysUserDetails newDetails = new SysUserDetails();
        newDetails.setUserId(details.getUserId());
        newDetails.setUsername(details.getUsername());
        newDetails.setDeptId(details.getDeptId());
        newDetails.setDataScopes(details.getDataScopes());
        newDetails.setTenantId(tenantId);
        newDetails.setCanSwitchTenant(details.getCanSwitchTenant());

        Authentication newAuth = new UsernamePasswordAuthenticationToken(newDetails, authentication.getCredentials(), authentication.getAuthorities());
        AuthenticationToken token = tokenManager.generateToken(newAuth);
        return Result.success(token);
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/login/sms")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGIN)
    public Result<AuthenticationToken> loginBySms(
            @Parameter(description = "手机号", example = "18888888888") @RequestParam String mobile,
            @Parameter(description = "验证码", example = "1234") @RequestParam String code
    ) {
        AuthenticationToken loginResult = authService.loginBySms(mobile, code);
        return Result.success(loginResult);
    }

    @Operation(summary = "发送登录短信验证码")
    @PostMapping("/sms/code")
    public Result<Void> sendLoginVerifyCode(
            @Parameter(description = "手机号", example = "18888888888") @RequestParam String mobile
    ) {
        authService.sendSmsLoginCode(mobile);
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @DeleteMapping("/logout")
    @Log(module = LogModuleEnum.LOGIN, value = ActionTypeEnum.LOGOUT)
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh-token")
    public Result<?> refreshToken(
            @Parameter(description = "刷新令牌", example = "xxx.xxx.xxx") @RequestParam String refreshToken
    ) {
        AuthenticationToken authenticationToken = authService.refreshToken(refreshToken);
        return Result.success(authenticationToken);
    }

}
