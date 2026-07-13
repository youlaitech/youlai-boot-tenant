package com.youlai.boot.system.controller;

import com.youlai.boot.auth.service.impl.WxMaAuthServiceImpl;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.system.model.form.AppForm;
import com.youlai.boot.system.model.form.AppStatusForm;
import com.youlai.boot.system.model.query.AppQuery;
import com.youlai.boot.system.model.vo.AppPageVO;
import com.youlai.boot.system.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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

/**
 * 应用管理控制器
 */
@Tag(name = "13.应用管理接口")
@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
@Slf4j
public class AppController {

    private final AppService appService;

    @Lazy
    private final WxMaAuthServiceImpl wxMaAuthService;

    @Operation(summary = "应用分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:app:list')")
    public PageResult<AppPageVO> getAppPage(AppQuery queryParams) {
        return PageResult.success(appService.getAppPage(queryParams));
    }

    @Operation(summary = "应用表单数据")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('sys:app:update')")
    public Result<AppForm> getAppForm(
            @Parameter(description = "应用ID") @PathVariable Long id
    ) {
        return Result.success(appService.getAppForm(id));
    }

    @Operation(summary = "新增应用")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:app:create')")
    public Result<Void> createApp(@Valid @RequestBody AppForm form) {
        boolean result = appService.saveApp(form);
        wxMaAuthService.reloadWxMaConfigs();
        return Result.judge(result);
    }

    @Operation(summary = "修改应用")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:app:update')")
    public Result<Void> updateApp(
            @Parameter(description = "应用ID") @PathVariable Long id,
            @Valid @RequestBody AppForm form
    ) {
        boolean result = appService.updateApp(id, form);
        wxMaAuthService.reloadWxMaConfigs();
        return Result.judge(result);
    }

    @Operation(summary = "删除应用")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:app:delete')")
    public Result<Void> deleteApps(
            @Parameter(description = "应用ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        appService.deleteApps(ids);
        wxMaAuthService.reloadWxMaConfigs();
        return Result.success();
    }

    @Operation(summary = "修改应用状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("@ss.hasPerm('sys:app:change-status')")
    public Result<Void> updateStatus(
            @Parameter(description = "应用ID") @PathVariable Long id,
            @Valid @RequestBody AppStatusForm form
    ) {
        boolean result = appService.updateStatus(id, form.getStatus());
        return Result.judge(result);
    }
}
