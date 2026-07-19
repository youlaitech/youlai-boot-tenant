package com.youlai.boot.framework.security.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.common.constant.SecurityConstants;
import com.youlai.boot.common.constant.SystemConstants;
import com.youlai.boot.framework.security.model.RoleDataScope;
import com.youlai.boot.framework.security.model.SecurityUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Security 工具类
 *
 * @author Ray
 * @since 2021/1/10
 */
public class SecurityUtils {

    /** 获取当前登录用户 */
    public static Optional<SecurityUserDetails> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof SecurityUserDetails) {
                return Optional.of((SecurityUserDetails) principal);
            }
        }
        return Optional.empty();
    }

    /** 获取当前用户ID */
    public static Long getUserId() {
        return getUser().map(SecurityUserDetails::getUserId).orElse(null);
    }

    /** 获取当前用户名 */
    public static String getUsername() {
        return getUser().map(SecurityUserDetails::getUsername).orElse(null);
    }

    /** 获取当前用户部门ID */
    public static Long getDeptId() {
        return getUser().map(SecurityUserDetails::getDeptId).orElse(null);
    }

    /** 获取当前用户数据权限范围 */
    public static List<RoleDataScope> getDataScopes() {
        return getUser().map(SecurityUserDetails::getDataScopes).orElse(Collections.emptyList());
    }

    /**
     * 当前用户是否可切换租户
     * <p>
     * 值为登录时从 UserDetails 解析：角色权限中包含 sys:tenant:switch 则为 true。
     * 见 {@link com.youlai.boot.system.service.impl.UserServiceImpl#resolveCanSwitchTenant}
     * </p>
     */
    public static boolean canSwitchTenant() {
        return getUser().map(SecurityUserDetails::getCanSwitchTenant).orElse(false);
    }

    /** 获取当前用户角色编码集合（去掉 ROLE_ 前缀） */
    public static Set<String> getRoles() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getAuthorities)
                .filter(CollectionUtil::isNotEmpty)
                .stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(SecurityConstants.ROLE_PREFIX))
                .map(authority -> StrUtil.removePrefix(authority, SecurityConstants.ROLE_PREFIX))
                .collect(Collectors.toSet());
    }

    /** 当前用户是否为超级管理员（ROOT） */
    public static boolean isRoot() {
        Set<String> roles = getRoles();
        return roles.contains(SystemConstants.ROOT_ROLE_CODE);
    }

    /** 从请求头获取 AccessToken */
    public static String getAccessToken() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (Objects.isNull(servletRequestAttributes)) {
            return null;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }

}