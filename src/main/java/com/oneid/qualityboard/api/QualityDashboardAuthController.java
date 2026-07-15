package com.oneid.qualityboard.api;

import com.youlai.boot.auth.controller.AuthController;
import com.youlai.boot.auth.model.LoginReq;
import com.youlai.boot.common.result.Result;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication entry point used only by the quality dashboard.
 */
@RestController
@RequestMapping("/api/v1/quality-auth")
@RequiredArgsConstructor
public class QualityDashboardAuthController {

    private final AuthController authController;

    @PostMapping("/login")
    public Result<?> login(HttpServletRequest httpServletRequest, @RequestBody @Valid LoginReq request) {
        return authController.login(httpServletRequest, request);
    }
}
