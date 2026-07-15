package com.oneid.qualityboard.security;

import com.youlai.boot.framework.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Stops unauthorized quality reads before their controller can query BigQuery.
 */
@Component
@RequiredArgsConstructor
public class QualityScopeInterceptor implements HandlerInterceptor {

    private final QualityScopeAccessService qualityScopeAccessService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required");
            return false;
        }

        String scope = request.getParameter("scope");
        String scopeId = request.getParameter("scope_id");
        if (!isValidScopePart(scope) || !isValidScopePart(scopeId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "scope and scope_id are required");
            return false;
        }

        String requestedScope = scope + ":" + scopeId;
        if (!qualityScopeAccessService.allowedScopes(userId, SecurityUtils.getRoles()).contains(requestedScope)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Quality scope is not granted");
            return false;
        }
        return true;
    }

    private boolean isValidScopePart(String value) {
        return value != null && !value.isBlank() && !value.contains(":") && value.equals(value.trim());
    }
}
