package com.youlai.boot.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 角色数据权限范围
 * <p>
 * 用于存储单个角色的数据权限信息，支持多角色数据权限合并（并集策略）。
 * 在用户登录时，将用户所有角色的数据权限信息加载到 Token 中，
 * 避免运行时频繁查询数据库。
 *
 * @author Ray.Hao
 * @since 2024/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDataScope implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 数据权限范围值
     * <ul>
     *   <li>1 - 全部数据</li>
     *   <li>2 - 部门及子部门数据</li>
     *   <li>3 - 本部门数据</li>
     *   <li>4 - 本人数据</li>
     *   <li>5 - 自定义部门数据</li>
     * </ul>
     */
    private Integer dataScope;

    /**
     * 自定义部门ID列表
     * <p>
     * 当 dataScope = 5（自定义）时，存储角色可访问的部门ID列表。
     * 通过 sys_role_dept 表关联获取。
     */
    private List<Long> customDeptIds;

    // ========== 静态工厂方法 ==========

    /**
     * 创建全部数据权限
     */
    public static RoleDataScope all(String roleCode) {
        return new RoleDataScope(roleCode, 1, null);
    }

    /**
     * 创建部门及子部门数据权限
     */
    public static RoleDataScope deptAndSub(String roleCode) {
        return new RoleDataScope(roleCode, 2, null);
    }

    /**
     * 创建本部门数据权限
     */
    public static RoleDataScope dept(String roleCode) {
        return new RoleDataScope(roleCode, 3, null);
    }

    /**
     * 创建本人数据权限
     */
    public static RoleDataScope self(String roleCode) {
        return new RoleDataScope(roleCode, 4, null);
    }

    /**
     * 创建自定义部门数据权限
     */
    public static RoleDataScope custom(String roleCode, List<Long> deptIds) {
        return new RoleDataScope(roleCode, 5, deptIds);
    }
}
