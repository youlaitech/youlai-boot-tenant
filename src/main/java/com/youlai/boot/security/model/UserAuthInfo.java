package com.youlai.boot.security.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 用户认证信息
 * <p>
 * 用于登录认证过程中的用户信息承载，包含用户名、密码、状态、角色等与认证/授权相关的数据。
 * 多租户模式下额外包含租户ID。
 * </p>
 *
 * @author Ray.Hao
 * @since 2025/12/16
 */
@Data
public class UserAuthInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 状态（1:启用 其它:禁用）
     */
    private Integer status;

    /**
     * 角色集合
     */
    private Set<String> roles;

    /**
     * 数据权限列表
     */
    private List<RoleDataScope> dataScopes;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 租户切换权限（true 可切换租户）
     */
    private Boolean canSwitchTenant;
}
