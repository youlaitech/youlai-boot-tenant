package com.youlai.boot.system.model.query;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "应用分页查询对象")
public class AppQuery extends BaseQuery {

    @Schema(description = "关键字(应用名称/编码/AppId)")
    private String keywords;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "状态(1-启用 0-停用)")
    private Integer status;
}
