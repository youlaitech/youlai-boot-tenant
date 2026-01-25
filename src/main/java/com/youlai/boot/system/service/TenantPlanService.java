package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.system.model.entity.TenantPlan;
import com.youlai.boot.system.model.form.TenantPlanForm;
import com.youlai.boot.system.model.query.TenantPlanQuery;
import com.youlai.boot.system.model.vo.TenantPlanPageVO;

import java.util.List;

/**
 * 租户方案业务接口
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
public interface TenantPlanService extends IService<TenantPlan> {

    /**
     * 租户方案分页列表
     *
     * @param queryParams 查询参数
     * @return 租户方案分页列表
     */
    IPage<TenantPlanPageVO> getTenantPlanPage(TenantPlanQuery queryParams);

    /**
     * 租户方案下拉列表
     *
     * @return 方案下拉列表
     */
    List<Option<Long>> listTenantPlanOptions();

    /**
     * 获取方案表单数据
     *
     * @param planId 方案ID
     * @return 方案表单数据
     */
    TenantPlanForm getTenantPlanForm(Long planId);

    /**
     * 新增方案
     *
     * @param formData 方案表单
     * @return 是否新增成功
     */
    boolean saveTenantPlan(TenantPlanForm formData);

    /**
     * 更新方案
     *
     * @param planId 方案ID
     * @param formData 方案表单
     * @return 是否更新成功
     */
    boolean updateTenantPlan(Long planId, TenantPlanForm formData);

    /**
     * 删除方案
     *
     * @param ids 方案ID，多个以英文逗号(,)分割
     */
    void deleteTenantPlans(String ids);

    /**
     * 获取方案菜单ID集合
     *
     * @param planId 方案ID
     * @return 菜单ID集合
     */
    List<Long> getTenantPlanMenuIds(Long planId);

    /**
     * 更新方案菜单
     *
     * @param planId 方案ID
     * @param menuIds 菜单ID集合
     */
    void updateTenantPlanMenus(Long planId, List<Long> menuIds);
}
