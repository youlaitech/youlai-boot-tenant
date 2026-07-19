package com.youlai.boot.system.security.adapter;

import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.framework.security.model.SecurityUser;
import com.youlai.boot.framework.security.port.UserAuthenticationPort;
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
/**
 * 用户认证信息适配器（实现 framework 端口）
 */
@Component
@RequiredArgsConstructor
public class UserAuthenticationAdapter implements UserAuthenticationPort {
    private final UserService userService;
    private final UserSocialService userSocialService;
    public SecurityUser getAuthInfoByUsername(String username) { return userService.getAuthInfoByUsername(username); }
    public SecurityUser getAuthInfoByMobile(String mobile) { return userService.getAuthInfoByMobile(mobile); }
    public SecurityUser getAuthInfoByOpenid(SocialPlatformEnum platform, String openid) { return userSocialService.getAuthInfoByOpenid(platform, openid); }
}