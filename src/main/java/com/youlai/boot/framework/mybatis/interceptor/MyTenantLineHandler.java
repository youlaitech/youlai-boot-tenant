package com.youlai.boot.framework.mybatis.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.youlai.boot.framework.tenant.TenantContextHolder;
import com.youlai.boot.framework.tenant.TenantProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 多租户处理器
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Component
@RequiredArgsConstructor
public class MyTenantLineHandler implements TenantLineHandler {

    private final TenantProperties tenantProperties;

    private Set<String> ignoreTableSet;

    private static final Set<String> SYSTEM_TABLES = Set.of(
            "tables",
            "columns"
    );

    @PostConstruct
    void init() {
        List<String> ignoreTables = tenantProperties.getIgnoreTables();
        ignoreTableSet = ignoreTables != null
                ? ignoreTables.stream().map(String::toLowerCase).collect(Collectors.toSet())
                : Set.of();
    }

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("租户ID为空，请确保在访问数据库前已设置租户上下文");
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return tenantProperties.getColumn();
    }
/**
 * 判断表是否跳过租户过滤
 */

    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null) {
            return false;
        }

        String lowerTableName = tableName.toLowerCase();

        if (SYSTEM_TABLES.contains(lowerTableName)) {
            return true;
        }

        if (TenantContextHolder.isIgnoreTenant()) {
            return true;
        }

        return ignoreTableSet.contains(lowerTableName);
    }
}