package com.youlai.boot.framework.tenant;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 基于 TTL 的租户上下文持有者
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
public class TenantContextHolder {

    private static final TransmittableThreadLocal<Long> TENANT_ID_HOLDER = new TransmittableThreadLocal<>();

    private static final TransmittableThreadLocal<Boolean> IGNORE_TENANT_HOLDER = new TransmittableThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        if (tenantId != null) {
            TENANT_ID_HOLDER.set(tenantId);
        }
    }

    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    public static void setIgnoreTenant(boolean ignore) {
        IGNORE_TENANT_HOLDER.set(ignore);
    }

    public static boolean isIgnoreTenant() {
        Boolean ignore = IGNORE_TENANT_HOLDER.get();
        return ignore != null && ignore;
    }

    public static void clear() {
        TENANT_ID_HOLDER.remove();
        IGNORE_TENANT_HOLDER.remove();
    }
}

