package com.oneid.qualityboard.api;

import com.oneid.qualityboard.security.QualityScopeAccessService;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.framework.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lists scopes from the server-side grants for the authenticated caller.
 */
@RestController
@RequestMapping("/api/v1/quality")
@RequiredArgsConstructor
public class QualityScopeController {

    private final QualityScopeAccessService qualityScopeAccessService;

    @GetMapping("/scopes")
    public Result<List<QualityScopeResponse>> getScopes() {
        List<QualityScopeResponse> scopes = qualityScopeAccessService
                .allowedScopes(SecurityUtils.getUserId(), SecurityUtils.getRoles())
                .stream()
                .sorted()
                .map(QualityScopeResponse::fromScopeKey)
                .toList();
        return Result.success(scopes);
    }
}
