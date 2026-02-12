package com.youlai.boot.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 在线用户信息对象
 *
 * @author wangtao
 * @since 2025/2/27 10:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUser {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 数据权限列表
     * <p>存储用户所有角色的数据权限范围，用于实现多角色权限合并</p>
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

    /**
     * 角色权限集合
     */
    private Set<String> roles;

}
