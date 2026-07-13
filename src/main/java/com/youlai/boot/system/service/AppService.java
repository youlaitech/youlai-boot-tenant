package com.youlai.boot.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.system.model.entity.App;
import com.youlai.boot.system.model.form.AppForm;
import com.youlai.boot.system.model.query.AppQuery;
import com.youlai.boot.system.model.vo.AppPageVO;

/**
 * 应用服务接口
 */
public interface AppService extends IService<App> {

    /**
     * 应用分页列表
     */
    Page<AppPageVO> getAppPage(AppQuery queryParams);

    /**
     * 应用表单数据
     */
    AppForm getAppForm(Long id);

    /**
     * 新增应用
     */
    boolean saveApp(AppForm form);

    /**
     * 修改应用
     */
    boolean updateApp(Long id, AppForm form);

    /**
     * 删除应用（逻辑删除）
     */
    void deleteApps(String ids);

    /**
     * 修改应用状态
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 按 AppId 查询应用（微信登录时解析租户与密钥）
     */
    App getByAppId(String appId);
}
