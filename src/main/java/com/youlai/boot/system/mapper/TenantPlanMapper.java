package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.system.model.entity.TenantPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户方案 Mapper
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Mapper
public interface TenantPlanMapper extends BaseMapper<TenantPlan> {
}
