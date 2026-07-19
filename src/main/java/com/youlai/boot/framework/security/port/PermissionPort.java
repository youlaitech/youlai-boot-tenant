package com.youlai.boot.framework.security.port;

import java.util.Set;
/**
 * 权限查询端口（framework 层获取角色权限集合）
 */
public interface PermissionPort {
    Set<String> getRolePerms(Set<String> roleCodes);
}