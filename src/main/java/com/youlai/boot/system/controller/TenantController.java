package com.youlai.boot.system.controller;

import com.youlai.boot.framework.tenant.TenantContextHolder;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.system.model.form.TenantCreateForm;
import com.youlai.boot.system.model.form.TenantForm;
import com.youlai.boot.system.model.query.TenantQuery;
import com.youlai.boot.system.model.vo.TenantCreateResultVO;
import com.youlai.boot.system.model.vo.TenantPageVO;
import com.youlai.boot.system.model.vo.TenantVO;
import com.youlai.boot.system.service.TenantService;
import com.youlai.boot.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户管理控制器
 * <p>
 * 提供租户切换、查询等功能
 * </p>
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@Tag(name = "14.租户管理接口")
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "获取当前用户可访问的租户列表")
    @GetMapping("/options")
    public Result<List<TenantVO>> getAccessibleTenants() {
        Long userId = SecurityUtils.getUserId();
        List<TenantVO> tenantList = tenantService.getAccessibleTenants(userId);
        return Result.success(tenantList);
    }

    /**
     * 获取当前租户信息
     *
     * @return 当前租户信息
     */
    @Operation(summary = "获取当前租户信息")
    @GetMapping("/current")
    public Result<TenantVO> getCurrentTenant() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return Result.success(null);
        }

        TenantVO tenant = tenantService.getTenantById(tenantId);
        return Result.success(tenant);
    }

    @Operation(summary = "租户分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:tenant:list')")
    public PageResult<TenantPageVO> getTenantPage(TenantQuery queryParams) {
        return PageResult.success(tenantService.getTenantPage(queryParams));
    }

    @Operation(summary = "获取租户表单数据")
    @GetMapping("/{tenantId}/form")
    @PreAuthorize("@ss.hasPerm('sys:tenant:update')")
    public Result<TenantForm> getTenantForm(
            @Parameter(description = "租户 ID") @PathVariable Long tenantId
    ) {
        TenantForm formData = tenantService.getTenantForm(tenantId);
        return Result.success(formData);
    }

    @Operation(summary = "新增租户并初始化默认数据")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:tenant:create')")
    public Result<TenantCreateResultVO> createTenant(@RequestBody @Valid TenantCreateForm form) {
        TenantCreateResultVO result = tenantService.createTenantWithInit(form);
        return Result.success(result);
    }

    @Operation(summary = "修改租户")
    @PutMapping("/{tenantId}")
    @PreAuthorize("@ss.hasPerm('sys:tenant:update')")
    public Result<?> updateTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @RequestBody @Valid TenantForm formData
    ) {
        boolean result = tenantService.updateTenant(tenantId, formData);
        return Result.judge(result);
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:tenant:delete')")
    public Result<Void> deleteTenants(
            @Parameter(description = "租户ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        tenantService.deleteTenants(ids);
        return Result.success();
    }

    @Operation(summary = "修改租户状态")
    @PutMapping("/{tenantId}/status")
    @PreAuthorize("@ss.hasPerm('sys:tenant:change-status')")
    public Result<?> updateTenantStatus(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = tenantService.updateTenantStatus(tenantId, status);
        return Result.judge(result);
    }

    @Operation(summary = "获取租户菜单ID集合")
    @GetMapping("/{tenantId}/menuIds")
    @PreAuthorize("@ss.hasPerm('sys:tenant:plan-assign')")
    public Result<List<Long>> getTenantMenuIds(
            @Parameter(description = "租户ID") @PathVariable Long tenantId
    ) {
        List<Long> menuIds = tenantService.getTenantMenuIds(tenantId);
        return Result.success(menuIds);
    }

    @Operation(summary = "更新租户菜单")
    @PutMapping("/{tenantId}/menus")
    @PreAuthorize("@ss.hasPerm('sys:tenant:plan-assign')")
    public Result<Void> updateTenantMenus(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @RequestBody List<Long> menuIds
    ) {
        tenantService.updateTenantMenus(tenantId, menuIds);
        return Result.success();
    }

    @Operation(summary = "切换租户")
    @PostMapping("/{tenantId}/switch")
    public Result<TenantVO> switchTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId
    ) {
        Long userId = SecurityUtils.getUserId();

        if (!tenantService.canAccessTenant(userId, tenantId)) {
            return Result.failed("无权访问该租户");
        }

        TenantVO tenant = tenantService.getTenantById(tenantId);
        if (tenant == null) {
            return Result.failed("租户不存在");
        }
        if (tenant.getStatus() == null || !StatusEnum.ENABLE.getValue().equals(tenant.getStatus())) {
            return Result.failed("租户已禁用");
        }

        TenantContextHolder.setTenantId(tenantId);
        return Result.success(tenant);
    }
}