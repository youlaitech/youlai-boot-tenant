package com.youlai.boot.system.service.impl;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMenuServiceImplTest {

    @Test
    void readsPermissionsFromLegacyTypedJsonCacheValue() {
        Set<String> permissions = new HashSet<>();

        RoleMenuServiceImpl.appendCachedPermissions(
                permissions,
                "[\"java.util.HashSet\",[\"sys:tenant:switch\",\"sys:user:list\"]]");

        assertThat(permissions).containsExactlyInAnyOrder("sys:tenant:switch", "sys:user:list");
    }

    @Test
    void readsPermissionsFromLegacyTypedCollectionCacheValue() {
        Set<String> permissions = new HashSet<>();

        RoleMenuServiceImpl.appendCachedPermissions(
                permissions,
                List.of("java.util.HashSet", List.of("sys:tenant:switch", "sys:user:list")));

        assertThat(permissions).containsExactlyInAnyOrder("sys:tenant:switch", "sys:user:list");
    }
}
