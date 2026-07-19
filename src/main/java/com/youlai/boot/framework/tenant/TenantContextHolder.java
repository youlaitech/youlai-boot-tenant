package com.youlai.boot.framework.tenant;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 基于 TTL 的租户上下文持有者
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
public class TenantContextHolder {

    /**
     * 当前请求的租户ID，随线程/父子线程传递
     */
    private static final TransmittableThreadLocal<Long> TENANT_ID_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 是否跳过租户过滤，用于跨租户查询（如扫码登录换取用户）
     */
    private static final TransmittableThreadLocal<Boolean> IGNORE_TENANT_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 设置当前租户ID（null 时忽略）
     */
    public static void setTenantId(Long tenantId) {
        if (tenantId != null) {
            TENANT_ID_HOLDER.set(tenantId);
        }
    }

    /**
     * 获取当前租户ID，未设置时返回 null
     */
    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 开启/关闭租户过滤跳过
     */
    public static void setIgnoreTenant(boolean ignore) {
        IGNORE_TENANT_HOLDER.set(ignore);
    }

    /**
     * 当前是否跳过租户过滤
     */
    public static boolean isIgnoreTenant() {
        Boolean ignore = IGNORE_TENANT_HOLDER.get();
        return ignore != null && ignore;
    }

    /**
     * 清空租户上下文，防止线程复用串租户
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
        IGNORE_TENANT_HOLDER.remove();
    }
}