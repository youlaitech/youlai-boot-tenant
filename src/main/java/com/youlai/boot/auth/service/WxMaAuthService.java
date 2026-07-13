package com.youlai.boot.auth.service;

import com.youlai.boot.auth.model.vo.WxMaLoginVO;
import com.youlai.boot.framework.security.model.AuthenticationToken;

/**
 * 微信小程序认证服务接口（多租户扩展：支持按 appId 解析归属租户）
 */
public interface WxMaAuthService {

    /**
     * 静默登录
     */
    WxMaLoginVO silentLogin(String code);

    /**
     * 静默登录（指定应用，用于解析归属租户与微信密钥）
     */
    WxMaLoginVO silentLogin(String code, String appId);

    /**
     * 手机号快捷登录
     */
    AuthenticationToken phoneLogin(String loginCode, String phoneCode);

    /**
     * 手机号快捷登录（指定应用）
     */
    AuthenticationToken phoneLogin(String loginCode, String phoneCode, String appId);

    /**
     * 绑定手机号
     */
    AuthenticationToken bindMobile(String openid, String mobile, String smsCode);

    /**
     * 绑定手机号（指定应用）
     */
    AuthenticationToken bindMobile(String openid, String mobile, String smsCode, String appId);
}
