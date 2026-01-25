package com.youlai.boot.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.system.model.entity.TenantPlan;
import com.youlai.boot.system.model.form.TenantPlanForm;
import com.youlai.boot.system.model.vo.TenantPlanPageVO;
import org.mapstruct.Mapper;

/**
 * 租户方案对象转换器
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Mapper(componentModel = "spring")
public interface TenantPlanConverter {

    Page<TenantPlanPageVO> toPageVo(Page<TenantPlan> page);

    TenantPlan toEntity(TenantPlanForm formData);

    TenantPlanForm toForm(TenantPlan entity);
}
