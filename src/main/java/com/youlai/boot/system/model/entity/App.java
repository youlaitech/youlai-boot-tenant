package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用实体（平台级，记录各渠道小程序的 AppId/密钥与归属租户）
 */
@Data
@TableName("sys_app")
public class App {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 应用名称 */
    private String appName;

    /** 应用编码 */
    private String appCode;

    /** 平台(wechat-mp/wechat-oa/alipay-mp) */
    private String platform;

    /** 微信/支付宝分配的 AppId */
    private String appId;

    /** 应用密钥 */
    private String appSecret;

    /** 商户号 */
    private String merchantId;

    /** 商户密钥 */
    private String merchantKey;

    /** 状态(1-启用 0-停用) */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 归属租户ID(0 表示平台级) */
    private Long tenantId;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    /** 逻辑删除(0-否 1-是) */
    private Integer isDeleted;
}
