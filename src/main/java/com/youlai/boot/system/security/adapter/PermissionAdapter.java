package com.youlai.boot.system.security.adapter;

import com.youlai.boot.framework.security.port.PermissionPort;
import com.youlai.boot.system.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionAdapter implements PermissionPort {
    private final RoleMenuService roleMenuService;
    public Set<String> getRolePerms(Set<String> roleCodes) { return roleMenuService.getRolePermsByRoleCodes(roleCodes); }
}