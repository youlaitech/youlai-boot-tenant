package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户方案菜单关联实体
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_tenant_plan_menu")
public class TenantPlanMenu {

    /**
     * 方案ID
     */
    private Long planId;

    /**
     * 菜单ID
     */
    private Long menuId;
}
