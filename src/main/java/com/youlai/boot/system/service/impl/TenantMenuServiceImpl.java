package com.youlai.boot.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.system.mapper.TenantMenuMapper;
import com.youlai.boot.system.mapper.TenantMapper;
import com.youlai.boot.system.model.entity.TenantMenu;
import com.youlai.boot.system.model.entity.Tenant;
import com.youlai.boot.system.service.TenantMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租户菜单服务实现类
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Service
@RequiredArgsConstructor
public class TenantMenuServiceImpl extends ServiceImpl<TenantMenuMapper, TenantMenu> implements TenantMenuService {

    private final TenantMapper tenantMapper;

    /**
     * 获取租户可用菜单ID集合
     *
     * @param tenantId 租户ID
     * @return 菜单ID集合
     */
    @Override
    public List<Long> listMenuIdsByTenant(Long tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return this.list(new LambdaQueryWrapper<TenantMenu>()
                        .select(TenantMenu::getMenuId)
                        .eq(TenantMenu::getTenantId, tenantId))
                .stream()
                .map(TenantMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 保存租户菜单配置
     *
     * @param tenantId 租户ID
     * @param menuIds 菜单ID集合
     */
    @Override
    public void saveTenantMenus(Long tenantId, List<Long> menuIds) {
        if (tenantId == null) {
            return;
        }

        this.remove(new LambdaQueryWrapper<TenantMenu>()
                .eq(TenantMenu::getTenantId, tenantId));

        if (CollectionUtil.isNotEmpty(menuIds)) {
            List<TenantMenu> tenantMenus = menuIds.stream()
                    .filter(Objects::nonNull)
                    .map(menuId -> new TenantMenu(tenantId, menuId))
                    .collect(Collectors.toList());
            this.saveBatch(tenantMenus);
        }
    }

    /**
     * 新增租户菜单时，将菜单关联到所有租户
     *
     * @param menuId 菜单ID
     */
    @Override
    public void addMenuToAllTenants(Long menuId) {
        if (menuId == null) {
            return;
        }
        // 检查是否已有关联（避免重复）
        long existingCount = this.count(new LambdaQueryWrapper<TenantMenu>()
                .eq(TenantMenu::getMenuId, menuId));
        if (existingCount > 0) {
            return;
        }
        // 获取所有租户ID，为每个租户创建关联
        List<Long> tenantIds = tenantMapper.selectList(new LambdaQueryWrapper<Tenant>()
                        .select(Tenant::getId))
                .stream()
                .map(Tenant::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtil.isNotEmpty(tenantIds)) {
            List<TenantMenu> tenantMenus = tenantIds.stream()
                    .map(tenantId -> new TenantMenu(tenantId, menuId))
                    .collect(Collectors.toList());
            this.saveBatch(tenantMenus);
        }
    }

    /**
     * 删除菜单时，清理所有租户与该菜单的关联
     *
     * @param menuId 菜单ID
     */
    @Override
    public void removeByMenuId(Long menuId) {
        if (menuId == null) {
            return;
        }
        this.remove(new LambdaQueryWrapper<TenantMenu>()
                .eq(TenantMenu::getMenuId, menuId));
    }
}
