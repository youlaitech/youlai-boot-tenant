package com.youlai.boot.auth.security.provider;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.framework.security.exception.CaptchaValidationException;
import com.youlai.boot.auth.security.model.SmsAuthenticationToken;
import com.youlai.boot.framework.security.model.SecurityUser;
import com.youlai.boot.framework.security.model.SecurityUserDetails;
import com.youlai.boot.framework.security.port.UserAuthenticationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final UserAuthenticationPort userAuthPort;
    private final RedisTemplate<String, Object> redisTemplate;

    public SmsAuthenticationProvider(UserAuthenticationPort userAuthPort, RedisTemplate<String, Object> redisTemplate) {
        this.userAuthPort = userAuthPort;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (String) authentication.getPrincipal();
        String inputVerifyCode = (String) authentication.getCredentials();
        if (StrUtil.isBlank(mobile)) { throw new CaptchaValidationException("手机号不能为空"); }
        if (StrUtil.isBlank(inputVerifyCode)) { throw new CaptchaValidationException("验证码不能为空"); }
        SecurityUser securityUser = userAuthPort.getAuthInfoByMobile(mobile);
        if (securityUser == null) { throw new UsernameNotFoundException("用户不存在"); }
        if (ObjectUtil.notEqual(securityUser.getStatus(), 1)) { throw new DisabledException("用户已被禁用"); }
        String cacheKey = StrUtil.format(RedisConstants.Captcha.SMS_LOGIN_CODE, mobile);
        String cachedVerifyCode = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedVerifyCode == null) { throw new CaptchaValidationException("验证码已过期"); }
        if (!StrUtil.equals(inputVerifyCode, cachedVerifyCode)) { throw new CaptchaValidationException("验证码错误"); }
        redisTemplate.delete(cacheKey);
        SecurityUserDetails userDetails = new SecurityUserDetails(securityUser);
        return SmsAuthenticationToken.authenticated(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
