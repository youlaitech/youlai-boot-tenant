package com.youlai.boot.plugin.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.youlai.boot.common.tenant.TenantContextHolder;
import com.youlai.boot.config.property.TenantProperties;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * mybatis-plus 字段自动填充
 * <p>
 * 支持自动填充创建时间、更新时间和租户ID
 * </p>
 *
 * @author Ray.Hao
 * @since 2022/10/14
 */
@Component
@RequiredArgsConstructor
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired(required = false)
    private TenantProperties tenantProperties;

    /**
     * 新增填充创建时间、更新时间和租户ID
     * <p>
     * 多租户模式下，tenant_id 字段的 exist 属性会被 TenantDynamicFieldConfig 动态设置为 true，
     * 因此这里的 strictInsertFill 可以正常工作
     * </p>
     *
     * @param metaObject 元数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);

        // 强制多租户：仅当上下文存在 tenantId 时自动填充
        if (tenantProperties != null) {
            Long tenantId = TenantContextHolder.getTenantId();
            if (tenantId != null) {
                Long finalTenantId = tenantId;
                this.strictInsertFill(metaObject, "tenantId", () -> finalTenantId, Long.class);
            }
        }
    }

    /**
     * 更新填充更新时间
     *
     * @param metaObject 元数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

}
