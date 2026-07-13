package com.youlai.boot.framework.security.port;

import java.util.Set;

public interface PermissionPort {
    Set<String> getRolePerms(Set<String> roleCodes);
}