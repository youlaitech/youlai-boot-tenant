package com.youlai.boot.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体
 */
@TableName("sys_user")
@Getter
@Setter
public class User extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别((1-男 2-女 0-保密)
     */
    private Integer gender;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 密码
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String password;

    /**
     * 部门 ID
     */
    private Long deptId;

    /**
     * 用户头像
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String avatar;

    /**
     * 联系方式
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String mobile;

    /**
     * 状态((1-正常 0-禁用)
     */
    private Integer status;

    /**
     * 用户邮箱
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String email;

    /**
     * 创建人 ID
     */
    private Long createBy;

    /**
     * 更新人 ID
     */
    private Long updateBy;

    /**
     * 是否删除(0-否 1-是)
     */
    private Integer isDeleted;
}