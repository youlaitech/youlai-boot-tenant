package com.youlai.boot.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 租户身份标识
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Getter
public enum TenantScopeEnum implements IBaseEnum<String> {

    PLATFORM("PLATFORM", "平台"),
    TENANT("TENANT", "租户");

    @EnumValue
    private final String value;

    private final String label;

    TenantScopeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
