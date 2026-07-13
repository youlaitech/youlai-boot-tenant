package com.youlai.boot.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "应用分页对象")
public class AppPageVO {

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "AppId")
    private String appId;

    @Schema(description = "商户号")
    private String merchantId;

    @Schema(description = "状态(1-启用 0-停用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "归属租户ID")
    private Long tenantId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
