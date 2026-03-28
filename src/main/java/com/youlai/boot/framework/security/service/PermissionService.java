package com.youlai.boot.framework.security.service;

import java.util.Set;

/**
 * 权限服务
 *
 * @author Ray
 */
public interface PermissionService {

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    Set<String> getRoleCodes(Long userId);

}
