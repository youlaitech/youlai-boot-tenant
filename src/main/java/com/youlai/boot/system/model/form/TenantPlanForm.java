package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 租户方案表单对象
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Data
@Schema(description = "租户方案表单")
public class TenantPlanForm {

    @Schema(description = "方案ID")
    private Long id;

    @Schema(description = "方案名称")
    @NotBlank(message = "方案名称不能为空")
    private String name;

    @Schema(description = "方案编码")
    @NotBlank(message = "方案编码不能为空")
    private String code;

    @Schema(description = "状态(1-启用 0-停用)")
    @Range(max = 1, min = 0, message = "方案状态不正确")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
