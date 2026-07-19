package com.youlai.boot.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 应用表单
 */
@Data
@Schema(description = "应用表单对象")
public class AppForm {

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "平台(wechat-mp/wechat-oa/alipay-mp)")
    private String platform;

    @Schema(description = "微信/支付宝分配的 AppId")
    private String appId;

    @Schema(description = "应用密钥")
    private String appSecret;

    @Schema(description = "商户号")
    private String merchantId;

    @Schema(description = "商户密钥")
    private String merchantKey;

    @Schema(description = "状态(1-启用 0-停用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "归属租户ID(0 表示平台级)")
    private Long tenantId;
}