package com.youlai.boot.framework.security.port;

import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.framework.security.model.SecurityUser;

public interface UserAuthenticationPort {
    SecurityUser getAuthInfoByUsername(String username);
    SecurityUser getAuthInfoByMobile(String mobile);
    SecurityUser getAuthInfoByOpenid(SocialPlatformEnum platform, String openid);
}