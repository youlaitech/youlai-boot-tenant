package com.youlai.boot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.system.mapper.AppMapper;
import com.youlai.boot.system.model.entity.App;
import com.youlai.boot.system.model.form.AppForm;
import com.youlai.boot.system.model.query.AppQuery;
import com.youlai.boot.system.model.vo.AppPageVO;
import com.youlai.boot.system.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Override
    public Page<AppPageVO> getAppPage(AppQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = StrUtil.trimToEmpty(queryParams.getKeywords());
        String platform = queryParams.getPlatform();
        Integer status = queryParams.getStatus();

        Page<App> page = this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<App>()
                        .and(StrUtil.isNotBlank(keywords), w -> w
                                .like(App::getAppName, keywords)
                                .or().like(App::getAppCode, keywords)
                                .or().like(App::getAppId, keywords))
                        .eq(platform != null, App::getPlatform, platform)
                        .eq(status != null, App::getStatus, status)
                        .eq(App::getIsDeleted, 0)
                        .orderByDesc(App::getCreateTime)
        );

        Page<AppPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return result;
    }

    @Override
    public AppForm getAppForm(Long id) {
        App app = this.getById(id);
        AppForm form = new AppForm();
        if (app != null) {
            BeanUtils.copyProperties(app, form);
        }
        return form;
    }
/**
 * 新增应用
 */

    @Override
    public boolean saveApp(AppForm form) {
        App app = new App();
        BeanUtils.copyProperties(form, app);
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        if (app.getTenantId() == null) {
            app.setTenantId(0L);
        }
        Long userId = SecurityUtils.getUserId();
        app.setCreateBy(userId);
        app.setCreateTime(LocalDateTime.now());
        app.setIsDeleted(0);
        return this.save(app);
    }
/**
 * 更新应用
 */

    @Override
    public boolean updateApp(Long id, AppForm form) {
        App app = this.getById(id);
        if (app == null) {
            return false;
        }
        BeanUtils.copyProperties(form, app, "id", "createBy", "createTime", "isDeleted");
        app.setId(id);
        Long userId = SecurityUtils.getUserId();
        app.setUpdateBy(userId);
        app.setUpdateTime(LocalDateTime.now());
        return this.updateById(app);
    }
/**
 * 删除应用
 */

    @Override
    public void deleteApps(String ids) {
        List<Long> idList = StrUtil.split(ids, ',').stream()
                .map(StrUtil::trimToEmpty)
                .filter(StrUtil::isNotBlank)
                .map(Long::valueOf)
                .toList();
        if (idList.isEmpty()) {
            return;
        }
        this.update(new LambdaUpdateWrapper<App>()
                .in(App::getId, idList)
                .set(App::getIsDeleted, 1)
                .set(App::getUpdateTime, LocalDateTime.now())
        );
    }
/**
 * 更新应用
 */

    @Override
    public boolean updateStatus(Long id, Integer status) {
        return this.update(new LambdaUpdateWrapper<App>()
                .eq(App::getId, id)
                .set(App::getStatus, status)
                .set(App::getUpdateTime, LocalDateTime.now())
        );
    }

    @Override
    public App getByAppId(String appId) {
        if (StrUtil.isBlank(appId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<App>()
                .eq(App::getAppId, appId)
                .eq(App::getIsDeleted, 0)
        );
    }

    private AppPageVO toVo(App app) {
        AppPageVO vo = new AppPageVO();
        BeanUtils.copyProperties(app, vo);
        return vo;
    }
}