package com.youlai.boot.framework.security.port;

import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.framework.security.model.SecurityUser;
/**
 * 用户认证信息端口（framework 层获取认证数据）
 */
public interface UserAuthenticationPort {
    SecurityUser getAuthInfoByUsername(String username);
    SecurityUser getAuthInfoByMobile(String mobile);
    SecurityUser getAuthInfoByOpenid(SocialPlatformEnum platform, String openid);
}