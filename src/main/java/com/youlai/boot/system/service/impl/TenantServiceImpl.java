package com.youlai.boot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.common.constant.SystemConstants;
import com.youlai.boot.common.tenant.TenantContextHolder;
import com.youlai.boot.core.exception.BusinessException;
import com.youlai.boot.security.model.SysUserDetails;
import com.youlai.boot.security.util.SecurityUtils;
import com.youlai.boot.system.converter.TenantConverter;
import com.youlai.boot.system.enums.TenantScopeEnum;
import com.youlai.boot.system.mapper.TenantMapper;
import com.youlai.boot.system.model.entity.Role;
import com.youlai.boot.system.model.entity.Tenant;
import com.youlai.boot.system.model.entity.User;
import com.youlai.boot.system.model.entity.Menu;
import com.youlai.boot.system.model.form.DeptForm;
import com.youlai.boot.system.model.form.RoleForm;
import com.youlai.boot.system.model.form.TenantCreateForm;
import com.youlai.boot.system.model.form.TenantForm;
import com.youlai.boot.system.model.form.UserForm;
import com.youlai.boot.system.model.query.TenantQuery;
import com.youlai.boot.system.model.vo.TenantCreateResultVO;
import com.youlai.boot.system.model.vo.TenantPageVO;
import com.youlai.boot.system.model.vo.TenantVO;
import com.youlai.boot.system.service.DeptService;
import com.youlai.boot.system.service.MenuService;
import com.youlai.boot.system.service.RoleService;
import com.youlai.boot.system.service.TenantService;
import com.youlai.boot.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 租户服务实现类
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {

    private final UserService userService;

    private final DeptService deptService;

    private final RoleService roleService;

    private final MenuService menuService;

    private final TenantConverter tenantConverter;

    @Override
    public boolean isPlatformTenantOperator() {
        String tenantScope = SecurityUtils.getTenantScope();
        if (tenantScope != null) {
            return TenantScopeEnum.PLATFORM.getValue().equalsIgnoreCase(tenantScope);
        }

        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }

        Long oldTenantId = TenantContextHolder.getTenantId();
        boolean oldIgnoreTenant = TenantContextHolder.isIgnoreTenant();
        try {
            TenantContextHolder.setIgnoreTenant(true);
            User user = userService.getById(userId);
            return user != null && TenantScopeEnum.PLATFORM.getValue().equalsIgnoreCase(user.getTenantScope());
        } finally {
            TenantContextHolder.setIgnoreTenant(oldIgnoreTenant);
            if (oldTenantId != null) {
                TenantContextHolder.setTenantId(oldTenantId);
            }
        }
    }

    private List<Long> resolveNewTenantAdminMenuIds() {
        Long oldTenantId = TenantContextHolder.getTenantId();
        boolean oldIgnoreTenant = TenantContextHolder.isIgnoreTenant();

        try {
            TenantContextHolder.setIgnoreTenant(false);
            TenantContextHolder.setTenantId(SystemConstants.DEFAULT_TENANT_ID);

            Role sourceRole = roleService.getOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getCode, SystemConstants.PLATFORM_ADMIN_ROLE_CODE)
                    .eq(Role::getIsDeleted, 0)
                    .last("LIMIT 1"));
            Assert.notNull(sourceRole, "默认租户未找到可用于复制的角色(ADMIN)");

            List<Long> sourceMenuIds = roleService.getRoleMenuIds(sourceRole.getId());
            if (sourceMenuIds == null || sourceMenuIds.isEmpty()) {
                // 兜底：如果默认租户 ADMIN 未配置 role_menu，则复制“所有非平台管理及其子级”的菜单
                return menuService.list(new LambdaQueryWrapper<Menu>()
                                .select(Menu::getId)
                                .ne(Menu::getId, SystemConstants.PLATFORM_MENU_ID)
                                .notLikeRight(Menu::getTreePath, "0," + SystemConstants.PLATFORM_MENU_ID)
                        )
                        .stream()
                        .map(Menu::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            List<Menu> menus = menuService.list(new LambdaQueryWrapper<Menu>()
                    .in(Menu::getId, sourceMenuIds));

            Set<Long> excludeIds = new HashSet<>();
            excludeIds.add(SystemConstants.PLATFORM_MENU_ID);

            if (menus != null && !menus.isEmpty()) {
                String platformTreePathPrefix = "0," + SystemConstants.PLATFORM_MENU_ID;
                for (Menu menu : menus) {
                    if (menu == null || menu.getId() == null) {
                        continue;
                    }
                    Long menuId = menu.getId();
                    if (SystemConstants.PLATFORM_MENU_ID.equals(menuId)) {
                        excludeIds.add(menuId);
                        continue;
                    }
                    String treePath = menu.getTreePath();
                    if (treePath == null) {
                        continue;
                    }
                    if (treePath.equals(platformTreePathPrefix) || treePath.startsWith(platformTreePathPrefix + ",")) {
                        excludeIds.add(menuId);
                    }
                }
            }

            return sourceMenuIds.stream()
                    .filter(Objects::nonNull)
                    .filter(id -> !excludeIds.contains(id))
                    .collect(Collectors.toList());
        } finally {
            TenantContextHolder.setIgnoreTenant(oldIgnoreTenant);
            if (oldTenantId != null) {
                TenantContextHolder.setTenantId(oldTenantId);
            }
        }
    }

    private String buildTenantAdminRoleCode(String tenantCode) {
        String code = StrUtil.toUpperCase(StrUtil.trimToEmpty(tenantCode));
        String raw = "TENANT_ADMIN_" + code;
        return StrUtil.maxLength(raw, 32);
    }

    private String buildTenantAdminUsername(String tenantCode) {
        String code = StrUtil.toLowerCase(StrUtil.trimToEmpty(tenantCode));
        String raw = "t_" + code + "_admin";
        return StrUtil.maxLength(raw, 64);
    }

    @Override
    public List<TenantVO> getAccessibleTenants(Long userId) {
        if (userId == null) {
            return List.of();
        }

        Long currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null) {
            return List.of();
        }

        // 非平台用户：仅允许访问当前租户
        if (!isPlatformTenantOperator()) {
            TenantVO tenant = getTenantById(currentTenantId);
            if (tenant == null || tenant.getStatus() == null || tenant.getStatus() != 1) {
                return List.of();
            }
            tenant.setIsDefault(true);
            return List.of(tenant);
        }

        // 平台用户：可访问所有启用租户（由后端权限控制平台账号本身，避免同名账号跨租户风险）
        TenantContextHolder.setIgnoreTenant(true);
        try {
            List<Tenant> tenants = this.list(
                    new LambdaQueryWrapper<Tenant>()
                            .eq(Tenant::getStatus, 1)
                            .orderByAsc(Tenant::getCreateTime)
                            .orderByAsc(Tenant::getId)
            );

            return tenants.stream()
                    .map(tenant -> {
                        TenantVO vo = new TenantVO();
                        BeanUtils.copyProperties(tenant, vo);
                        vo.setIsDefault(SystemConstants.DEFAULT_TENANT_ID.equals(tenant.getId()));
                        return vo;
                    })
                    .collect(Collectors.toList());
        } finally {
            TenantContextHolder.setIgnoreTenant(false);
        }
    }

    /**
     * 获取租户信息
     * @param tenantId 租户ID
     * @return 租户信息
     */
    @Override
    public TenantVO getTenantById(Long tenantId) {
        Tenant tenant = this.getById(tenantId);
        if (tenant == null) {
            return null;
        }
        TenantVO vo = new TenantVO();
        BeanUtils.copyProperties(tenant, vo);
        return vo;
    }

    /**
     * 根据域名获取租户ID
     * @param domain 租户域名
     * @return 租户ID
     */
    @Override
    public Long getTenantIdByDomain(String domain) {
        Tenant tenant = this.getOne(
                new LambdaQueryWrapper<Tenant>()
                        .eq(Tenant::getDomain, domain)
                        .eq(Tenant::getStatus, 1)
                        .last("LIMIT 1")
        );
        return tenant != null ? tenant.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCreateResultVO createTenantWithInit(TenantCreateForm form) {
        Assert.notNull(form, "请求参数不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许新增租户");

        String tenantName = StrUtil.trimToEmpty(form.getName());
        String tenantCode = StrUtil.toUpperCase(StrUtil.trimToEmpty(form.getCode()));

        Assert.isTrue(StrUtil.isNotBlank(tenantName), "租户名称不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tenantCode), "租户编码不能为空");

        long tenantCodeCount = this.count(new LambdaQueryWrapper<Tenant>().eq(Tenant::getCode, tenantCode));
        Assert.isTrue(tenantCodeCount == 0, "租户编码已存在");

        if (StrUtil.isNotBlank(form.getDomain())) {
            long domainCount = this.count(new LambdaQueryWrapper<Tenant>().eq(Tenant::getDomain, form.getDomain()));
            Assert.isTrue(domainCount == 0, "租户域名已存在");
        }

        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setCode(tenantCode);
        tenant.setContactName(form.getContactName());
        tenant.setContactPhone(form.getContactPhone());
        tenant.setContactEmail(form.getContactEmail());
        tenant.setDomain(form.getDomain());
        tenant.setLogo(form.getLogo());
        tenant.setRemark(form.getRemark());
        tenant.setExpireTime(form.getExpireTime());
        tenant.setStatus(1);

        boolean saved = this.save(tenant);
        Assert.isTrue(saved, "租户创建失败");
        Assert.notNull(tenant.getId(), "租户创建失败：未生成租户ID");

        Long newTenantId = tenant.getId();

        Long oldTenantId = TenantContextHolder.getTenantId();
        boolean oldIgnoreTenant = TenantContextHolder.isIgnoreTenant();
        try {
            TenantContextHolder.setIgnoreTenant(false);
            TenantContextHolder.setTenantId(newTenantId);

            // 1) 默认部门
            DeptForm deptForm = new DeptForm();
            deptForm.setParentId(SystemConstants.ROOT_NODE_ID);
            deptForm.setName(tenantName);
            deptForm.setCode(tenantCode);
            deptForm.setStatus(1);
            deptForm.setSort(1);
            Long deptId = deptService.saveDept(deptForm);

            // 2) 租户管理员角色（code/name 全局唯一）
            String roleCode = buildTenantAdminRoleCode(tenantCode);
            String roleName = StrUtil.maxLength("租户管理员-" + tenantCode, 64);

            RoleForm roleForm = new RoleForm();
            roleForm.setCode(roleCode);
            roleForm.setName(roleName);
            roleForm.setSort(1);
            roleForm.setStatus(1);
            roleForm.setDataScope(1);
            boolean roleSaved = roleService.saveRole(roleForm);
            Assert.isTrue(roleSaved, "租户管理员角色创建失败");

            Role role = roleService.getOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getCode, roleCode)
                    .eq(Role::getIsDeleted, 0)
                    .last("LIMIT 1"));
            Assert.notNull(role, "租户管理员角色创建失败：未查询到角色");
            Assert.notNull(role.getId(), "租户管理员角色创建失败：未生成角色ID");

            // 3) 租户管理员用户（用户名不使用 admin/root）
            String adminUsername = StrUtil.trimToEmpty(form.getAdminUsername());
            if (StrUtil.isBlank(adminUsername)) {
                adminUsername = buildTenantAdminUsername(tenantCode);
            }

            if (SystemConstants.PLATFORM_ROOT_USERNAME.equalsIgnoreCase(adminUsername)) {
                throw new BusinessException("租户管理员用户名不允许使用平台保留账号");
            }

            UserForm userForm = new UserForm();
            userForm.setUsername(adminUsername);
            userForm.setNickname(StrUtil.maxLength(tenantName + "管理员", 64));
            userForm.setStatus(1);
            userForm.setDeptId(deptId);
            userForm.setRoleIds(List.of(role.getId()));
            boolean userSaved = userService.saveUser(userForm);
            Assert.isTrue(userSaved, "租户管理员用户创建失败");

            // 4) 角色菜单授权（租户侧权限，不含平台管理）
            List<Long> menuIds = resolveNewTenantAdminMenuIds();
            roleService.assignMenusToRole(role.getId(), menuIds);

            TenantCreateResultVO resultVo = new TenantCreateResultVO();
            resultVo.setTenantId(newTenantId);
            resultVo.setTenantCode(tenantCode);
            resultVo.setTenantName(tenantName);
            resultVo.setAdminUsername(adminUsername);
            resultVo.setAdminInitialPassword(SystemConstants.DEFAULT_PASSWORD);
            resultVo.setAdminRoleCode(roleCode);
            return resultVo;
        } finally {
            TenantContextHolder.setIgnoreTenant(oldIgnoreTenant);
            if (oldTenantId != null) {
                TenantContextHolder.setTenantId(oldTenantId);
            }
        }
    }

    @Override
    public boolean canAccessTenant(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return false;
        }

        Long currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null) {
            return false;
        }

        // 非平台用户：仅允许访问当前租户
        if (!isPlatformTenantOperator()) {
            return tenantId.equals(currentTenantId);
        }

        TenantContextHolder.setIgnoreTenant(true);
        try {
            // 平台用户：仅允许切换到启用租户
            Tenant tenant = this.getById(tenantId);
            return tenant != null && tenant.getStatus() != null && tenant.getStatus() == 1;
        } finally {
            TenantContextHolder.setIgnoreTenant(false);
        }
    }

    @Override
    public Page<TenantPageVO> getTenantPage(TenantQuery queryParams) {
        Assert.notNull(queryParams, "请求参数不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许查询租户列表");

        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = StrUtil.trimToEmpty(queryParams.getKeywords());
        Integer status = queryParams.getStatus();

        Page<Tenant> page = this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Tenant>()
                        .and(StrUtil.isNotBlank(keywords), wrapper ->
                                wrapper.like(Tenant::getName, keywords)
                                        .or().like(Tenant::getCode, keywords)
                                        .or().like(Tenant::getDomain, keywords)
                        )
                        .eq(status != null, Tenant::getStatus, status)
                        .orderByAsc(Tenant::getCreateTime)
                        .orderByAsc(Tenant::getId)
        );

        return tenantConverter.toPageVo(page);
    }

    @Override
    public TenantForm getTenantForm(Long tenantId) {
        Assert.notNull(tenantId, "租户ID不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许查看租户信息");

        Tenant tenant = this.getById(tenantId);
        Assert.isTrue(tenant != null, "租户不存在");
        return tenantConverter.toForm(tenant);
    }

    @Override
    public boolean updateTenant(Long tenantId, TenantForm formData) {
        Assert.notNull(tenantId, "租户ID不能为空");
        Assert.notNull(formData, "请求参数不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许修改租户");

        Tenant old = this.getById(tenantId);
        Assert.isTrue(old != null, "租户不存在");

        String tenantName = StrUtil.trimToEmpty(formData.getName());
        String tenantCode = StrUtil.toUpperCase(StrUtil.trimToEmpty(formData.getCode()));
        String domain = StrUtil.trimToEmpty(formData.getDomain());
        if (StrUtil.isBlank(domain)) {
            domain = null;
        }

        Assert.isTrue(StrUtil.isNotBlank(tenantName), "租户名称不能为空");
        Assert.isTrue(StrUtil.isNotBlank(tenantCode), "租户编码不能为空");

        long tenantCodeCount = this.count(new LambdaQueryWrapper<Tenant>()
                .ne(Tenant::getId, tenantId)
                .eq(Tenant::getCode, tenantCode)
        );
        Assert.isTrue(tenantCodeCount == 0, "租户编码已存在");

        if (domain != null) {
            long domainCount = this.count(new LambdaQueryWrapper<Tenant>()
                    .ne(Tenant::getId, tenantId)
                    .eq(Tenant::getDomain, domain)
            );
            Assert.isTrue(domainCount == 0, "租户域名已存在");
        }

        Tenant tenant = tenantConverter.toEntity(formData);
        tenant.setId(tenantId);
        tenant.setName(tenantName);
        tenant.setCode(tenantCode);
        tenant.setDomain(domain);

        return this.updateById(tenant);
    }

    @Override
    public void deleteTenants(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的租户ID不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许删除租户");

        List<Long> tenantIds = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        for (Long tenantId : tenantIds) {
            Tenant tenant = this.getById(tenantId);
            Assert.isTrue(tenant != null, "租户不存在");

            Assert.isTrue(!SystemConstants.DEFAULT_TENANT_ID.equals(tenantId), "默认租户不允许删除");

            long userCount = userService.count(new LambdaQueryWrapper<User>()
                    .eq(User::getTenantId, tenantId)
                    .eq(User::getIsDeleted, 0)
            );
            Assert.isTrue(userCount == 0, "租户下存在用户，无法删除");

            boolean removed = this.removeById(tenantId);
            Assert.isTrue(removed, "租户删除失败");
        }
    }

    @Override
    public boolean updateTenantStatus(Long tenantId, Integer status) {
        Assert.notNull(tenantId, "租户ID不能为空");
        Assert.notNull(status, "状态不能为空");
        Assert.isTrue(isPlatformTenantOperator(), "仅平台管理员允许修改租户状态");

        Tenant tenant = this.getById(tenantId);
        Assert.isTrue(tenant != null, "租户不存在");

        if (SystemConstants.DEFAULT_TENANT_ID.equals(tenantId) && status == 0) {
            throw new BusinessException("默认租户不允许禁用");
        }

        tenant.setStatus(status);
        return this.updateById(tenant);
    }

}
