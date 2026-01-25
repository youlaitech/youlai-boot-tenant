package com.youlai.boot.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 多租户动态字段配置
 * <p>
 * 在多租户模式启用时，动态修改 BaseEntity 中 tenant_id 字段的 exist 属性为 true
 * 这样可以实现：
 * - 单租户模式：tenant_id exist=false，不映射该字段，兼容没有该字段的表
 * - 多租户模式：tenant_id exist=true，自动填充租户ID到INSERT/UPDATE语句
 * </p>
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Slf4j
public class TenantDynamicFieldConfig {
    // 单租户兼容逻辑已移除：多租户场景直接在实体中声明 tenantId 字段
}
