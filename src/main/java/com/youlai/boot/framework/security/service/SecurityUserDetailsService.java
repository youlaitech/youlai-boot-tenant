package com.youlai.boot.framework.security.service;

import com.youlai.boot.framework.security.model.SecurityUserDetails;
import com.youlai.boot.framework.security.model.SecurityUser;
import com.youlai.boot.framework.security.port.UserAuthenticationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 系统用户认证 DetailsService
 *
 * @author Ray.Hao
 * @since 2021/10/19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserAuthenticationPort userAuthPort;

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            SecurityUser userAuthInfo = userAuthPort.getAuthInfoByUsername(username);
            if (userAuthInfo == null) {
                throw new UsernameNotFoundException(username);
            }
            return new SecurityUserDetails(userAuthInfo);
        } catch (Exception e) {
            // 记录异常日志
            log.error("认证异常:{}", e.getMessage());
            // 抛出异常
            throw e;
        }
    }

    /**
     * 根据用户名获取用户详情
     *
     * @param username 用户名
     * @return 用户详情
     */
    public SecurityUserDetails getUserDetails(String username) {
        return (SecurityUserDetails) loadUserByUsername(username);
    }

}