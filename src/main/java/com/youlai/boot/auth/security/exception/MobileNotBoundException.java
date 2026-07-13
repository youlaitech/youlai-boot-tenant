package com.youlai.boot.auth.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 需要绑定手机号异常
 */
public class MobileNotBoundException extends AuthenticationException {

    private final String openid;

    private final String sessionKey;

    public MobileNotBoundException(String openid, String sessionKey) {
        super("需要绑定手机号");
        this.openid = openid;
        this.sessionKey = sessionKey;
    }

    public String getOpenid() {
        return openid;
    }

    public String getSessionKey() {
        return sessionKey;
    }

}
