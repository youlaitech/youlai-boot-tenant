package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.system.model.entity.TenantPlanMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户方案菜单关联 Mapper
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Mapper
public interface TenantPlanMenuMapper extends BaseMapper<TenantPlanMenu> {
}
