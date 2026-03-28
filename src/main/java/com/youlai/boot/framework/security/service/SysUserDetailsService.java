package com.youlai.boot.framework.security.service;

import com.youlai.boot.framework.security.model.SysUserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户详情服务
 *
 * @author Ray
 */
public interface SysUserDetailsService extends UserDetailsService {

    /**
     * 根据用户名获取用户详情
     *
     * @param username 用户名
     * @return 用户详情
     */
    SysUserDetails getUserDetails(String username);

}
