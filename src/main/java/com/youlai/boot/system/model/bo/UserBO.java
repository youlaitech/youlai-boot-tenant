package com.youlai.boot.system.model.bo;

import lombok.Data;

/**
 * 用户分页/详情展示对象
 *
 * @author Ray
 * @since 3.0.0
 */
@Data
public class UserBo {

    private Long id;

    private String username;

    private String nickname;

    private String mobile;

    private Integer gender;

    private String avatar;

    private Integer status;

    private String email;

    private String deptName;

    /**
     * 逗号分隔的角色名称列表
     */
    private String roleNames;

    private String createTime;
}


