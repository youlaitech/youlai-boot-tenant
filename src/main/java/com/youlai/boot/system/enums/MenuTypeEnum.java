package com.youlai.boot.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author Ray.Hao
 * @since 0.0.1
 */
@Getter
public enum MenuTypeEnum implements IBaseEnum<String> {

    CATALOG("C", "目录"),
    MENU("M", "菜单"),
    EXTERNAL("E", "外链"),
    BUTTON("B", "按钮");

    @EnumValue
    private final String value;

    private final String label;

    MenuTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
