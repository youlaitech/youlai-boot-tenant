package com.youlai.boot.plugin.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.youlai.boot.common.tenant.TenantContextHolder;
import com.youlai.boot.config.property.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * MyBatis-Plus 多租户处理器
 * <p>
 * 实现 TenantLineHandler 接口，自动为 SQL 添加租户过滤条件
 * </p>
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyTenantLineHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    /**
     * 获取租户ID表达式
     * <p>
     * 从 TenantContextHolder 获取当前租户ID
     * 如果未设置或忽略租户，抛出异常
     * </p>
     *
     * @return 租户ID表达式
     */
    @Override
    public Expression getTenantId() {
        log.debug("getTenantId() 被调用");

        // 获取当前租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        log.debug("从 TenantContextHolder 获取租户ID: {}", tenantId);

        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required but was null. Ensure TenantContextHolder is set (e.g., via token) before DB access.");
        }

        return new LongValue(tenantId);
    }

    /**
     * 获取租户字段名
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return tenantProperties.getColumn();
    }

    /**
     * 判断表是否忽略多租户过滤
     * <p>
     * 系统表、租户表等不需要租户隔离的表应返回 true
     * </p>
     *
     * @param tableName 表名
     * @return true-忽略，false-不忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return false;
        }

        Set<String> systemTables = Set.of(
                "tables",
                "columns",
                "all_tables",
                "all_tab_comments",
                "all_objects",
                "all_tab_columns",
                "all_col_comments",
                "all_cons_columns",
                "all_constraints"
        );
        if (systemTables.contains(tableName.toLowerCase())) {
            return true;
        }

        // 如果设置了忽略租户标志，则本次查询全部表都跳过租户过滤
        if (TenantContextHolder.isIgnoreTenant()) {
            return true;
        }

        List<String> ignoreTables = tenantProperties.getIgnoreTables();
        if (ignoreTables == null || ignoreTables.isEmpty()) {
            return false;
        }

        // 忽略表名匹配（不区分大小写）
        return ignoreTables.stream()
                .anyMatch(ignoreTable -> ignoreTable.equalsIgnoreCase(tableName));
    }
}

