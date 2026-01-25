package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.TenantPlanMenu;

import java.util.List;

/**
 * 租户方案菜单业务接口
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
public interface TenantPlanMenuService extends IService<TenantPlanMenu> {

    /**
     * 获取方案菜单ID集合
     *
     * @param planId 方案ID
     * @return 菜单ID集合
     */
    List<Long> listMenuIdsByPlan(Long planId);

    /**
     * 保存方案菜单配置
     *
     * @param planId 方案ID
     * @param menuIds 菜单ID集合
     */
    void savePlanMenus(Long planId, List<Long> menuIds);
}
