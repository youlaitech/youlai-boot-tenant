package com.youlai.boot.system.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户方案分页查询对象
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "租户方案分页查询对象")
public class TenantPlanQuery extends BaseQuery {

    @Schema(description = "关键字(方案名称/方案编码)")
    private String keywords;

    @Schema(description = "方案状态(1-启用 0-停用)")
    private Integer status;
}
