package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 * 应用状态表单
 */
@Schema(description = "应用状态表单对象")
@Data
public class AppStatusForm {

    @Schema(description = "状态(1-启用；0-禁用)")
    @NotNull(message = "状态不能为空")
    private Integer status;
}