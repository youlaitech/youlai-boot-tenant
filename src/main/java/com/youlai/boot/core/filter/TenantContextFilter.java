package com.youlai.boot.core.filter;

import com.youlai.boot.common.constant.SecurityConstants;
import com.youlai.boot.common.tenant.TenantContextHolder;
import com.youlai.boot.security.model.SysUserDetails;
import com.youlai.boot.security.token.TokenManager;
import com.youlai.boot.system.service.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户上下文过滤器
 * <p>
 * 从 Token 中解析租户ID，设置到线程上下文
 * 请求结束时自动清除上下文，避免线程池复用导致的数据泄露
 * </p>
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Slf4j
@Component
@Order(1) // 确保在其他过滤器之前执行
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final TokenManager tokenManager;

    private final TenantService tenantService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 1) 从已认证用户中获取租户ID（已登录态：只信 token/auth）
            Long tenantIdFromAuth = resolveTenantFromAuthentication(authentication);

            // 2) 如果尚未获取到认证信息，尝试从 Token 中解析（不写入 SecurityContext，仅用于解析 tenantId/username）
            Authentication authenticationFromToken = null;
            if (tenantIdFromAuth == null) {
                authenticationFromToken = resolveAuthenticationFromToken(request);
                tenantIdFromAuth = resolveTenantFromAuthentication(authenticationFromToken);
            }

            Long tenantId = tenantIdFromAuth;

            // 3) 未认证且无 token：从域名解析租户ID（子域名/域名映射场景）
            if (tenantId == null) {
                String domain = request != null ? request.getServerName() : null;
                if (StringUtils.hasText(domain)) {
                    tenantId = tenantService.getTenantIdByDomain(domain);
                }
            }

            if (tenantId != null) {
                TenantContextHolder.setTenantId(tenantId);
                log.debug("TenantContextFilter set tenantId: {}", tenantId);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Long resolveTenantFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SysUserDetails details) {
            return details.getTenantId();
        }
        return null;
    }

    private Authentication resolveAuthenticationFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX)) {
            return null;
        }
        String token = authHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
        try {
            return tokenManager.parseToken(token);
        } catch (Exception e) {
            log.warn("TenantContextFilter parseToken failed: {}", e.getMessage());
            return null;
        }
    }
}
