package com.youlai.boot.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.auth.model.vo.WxMaLoginVO;
import com.youlai.boot.auth.service.WxMaAuthService;
import com.youlai.boot.auth.security.exception.MobileNotBoundException;
import com.youlai.boot.auth.security.model.WxMaAuthenticationToken;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.common.constant.SystemConstants;
import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.framework.security.model.SecurityUserDetails;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.system.model.entity.App;
import com.youlai.boot.system.model.entity.User;
import com.youlai.boot.system.service.AppService;
import com.youlai.boot.system.service.UserRoleService;
import com.youlai.boot.system.service.UserService;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序认证服务（多租户）
 * 登录时按前端传入的 appId 从 sys_app 解析归属租户与微信密钥。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WxMaAuthServiceImpl implements WxMaAuthService {

    private final WxMaService wxMaService;
    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;
    private final UserService userService;
    private final UserSocialService userSocialService;
    private final UserRoleService userRoleService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AppService appService;

    /** 已加载的微信应用配置（appId -> config），由 sys_app 表驱动 */
    private final Map<String, WxMaConfig> wxMaConfigMap = new ConcurrentHashMap<>();
    private volatile boolean configReady = false;
    private volatile String defaultAppId;

    @PostConstruct
    public void initWxMaConfigs() {
        reloadWxMaConfigs();
    }

    /**
     * 从 yml 默认配置与 sys_app 表加载多应用配置。
     * 加载失败（如表未初始化）不阻塞启动，下次登录时会重试。
     */
    public synchronized void reloadWxMaConfigs() {
        try {
            Map<String, WxMaConfig> map = new HashMap<>();

            try {
                WxMaConfig ymlConfig = wxMaService.getWxMaConfig();
                if (ymlConfig != null && StrUtil.isNotBlank(ymlConfig.getAppid())) {
                    map.put(ymlConfig.getAppid(), ymlConfig);
                }
            } catch (Exception ignored) {
                // 多应用模式下尚未切换，忽略
            }

            List<App> apps = appService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<App>()
                            .eq(App::getStatus, 1)
                            .eq(App::getIsDeleted, 0)
            );
            for (App app : apps) {
                if (StrUtil.isBlank(app.getAppId())) {
                    continue;
                }
                WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
                config.setAppid(app.getAppId());
                config.setSecret(app.getAppSecret());
                map.put(app.getAppId(), config);
            }

            if (!map.isEmpty()) {
                wxMaService.setMultiConfigs(map);
                defaultAppId = map.keySet().stream().findFirst().orElse(null);
            }
            configReady = true;
        } catch (Exception e) {
            log.warn("加载微信应用配置失败，登录时将重试：{}", e.getMessage());
            configReady = false;
        }
    }

    private void ensureConfigs() {
        if (!configReady) {
            synchronized (this) {
                if (!configReady) {
                    reloadWxMaConfigs();
                }
            }
        }
    }

    /**
     * 按 appId 解析应用；未传 appId 时取默认启用应用。
     */
    private App resolveApp(String appId) {
        ensureConfigs();
        if (StrUtil.isNotBlank(appId)) {
            App app = appService.getByAppId(appId);
            if (app != null && Integer.valueOf(1).equals(app.getStatus())) {
                return app;
            }
        }
        if (StrUtil.isNotBlank(defaultAppId)) {
            return appService.getByAppId(defaultAppId);
        }
        return null;
    }

    private String resolveAppId(String appId) {
        App app = resolveApp(appId);
        if (app == null || StrUtil.isBlank(app.getAppId())) {
            throw new IllegalArgumentException("未配置可用的微信小程序应用");
        }
        return app.getAppId();
    }

    private Long resolveTenantId(App app) {
        if (app != null && app.getTenantId() != null && app.getTenantId() != 0) {
            return app.getTenantId();
        }
        return SystemConstants.DEFAULT_TENANT_ID;
    }

    @Override
    public WxMaLoginVO silentLogin(String code) {
        return silentLogin(code, null);
    }

    @Override
    public WxMaLoginVO silentLogin(String code, String appId) {
        String effectiveAppId = resolveAppId(appId);
        wxMaService.switchoverTo(effectiveAppId);

        WxMaAuthenticationToken token = new WxMaAuthenticationToken(code);
        try {
            Authentication authentication = authenticationManager.authenticate(token);
            AuthenticationToken authToken = tokenManager.generateToken(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return WxMaLoginVO.builder()
                    .isNewUser(false)
                    .needBindMobile(false)
                    .accessToken(authToken.getAccessToken())
                    .refreshToken(authToken.getRefreshToken())
                    .tokenType(authToken.getTokenType())
                    .expiresIn(authToken.getExpiresIn())
                    .build();
        } catch (MobileNotBoundException e) {
            return WxMaLoginVO.builder()
                    .isNewUser(true)
                    .needBindMobile(true)
                    .openid(e.getOpenid())
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken phoneLogin(String loginCode, String phoneCode) {
        return phoneLogin(loginCode, phoneCode, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken phoneLogin(String loginCode, String phoneCode, String appId) {
        App app = resolveApp(appId);
        Long tenantId = resolveTenantId(app);
        wxMaService.switchoverTo(resolveAppId(appId));

        WxMaJscode2SessionResult session = resolveSession(loginCode);
        String openid = session.getOpenid();

        String mobile = resolvePhoneNumber(phoneCode);

        log.info("微信小程序手机号快捷登录：appId={}, openid={}, mobile={}", app != null ? app.getAppId() : null, openid, mobile);

        User user = findOrCreateUser(mobile, tenantId);

        bindWechatOpenid(user, session);

        return generateAuthToken(mobile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken bindMobile(String openid, String mobile, String smsCode) {
        return bindMobile(openid, mobile, smsCode, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken bindMobile(String openid, String mobile, String smsCode, String appId) {
        App app = resolveApp(appId);
        Long tenantId = resolveTenantId(app);

        validateSmsCode(mobile, smsCode);

        User user = findOrCreateUser(mobile, tenantId);

        userSocialService.bindOrUpdate(
                user.getId(),
                SocialPlatformEnum.WECHAT_MINI,
                openid,
                null, null, null, null
        );

        log.info("微信小程序绑定手机号成功：appId={}, mobile={}, openid={}", app != null ? app.getAppId() : null, mobile, openid);

        return generateAuthToken(mobile);
    }

    // ==================== 私有方法 ====================

    private WxMaJscode2SessionResult resolveSession(String loginCode) {
        try {
            return wxMaService.jsCode2SessionInfo(loginCode);
        } catch (Exception e) {
            log.error("获取微信会话信息失败，loginCode={}", loginCode, e);
            throw new IllegalArgumentException("微信登录失败：" + e.getMessage());
        }
    }

    private String resolvePhoneNumber(String phoneCode) {
        try {
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(phoneCode);
            return phoneInfo.getPhoneNumber();
        } catch (Exception e) {
            log.error("获取微信手机号失败，phoneCode={}", phoneCode, e);
            throw new IllegalArgumentException("获取手机号失败：" + e.getMessage());
        }
    }

    private User findOrCreateUser(String mobile, Long tenantId) {
        User user = userService.lambdaQuery()
                .eq(User::getMobile, mobile)
                .one();

        if (user == null) {
            user = createNewUser(mobile, tenantId);
            log.info("微信小程序登录：创建新用户，mobile={}, tenantId={}, userId={}", mobile, tenantId, user.getId());
        }

        return user;
    }

    private User createNewUser(String mobile, Long tenantId) {
        User user = new User();
        user.setMobile(mobile);
        user.setUsername("wx_" + IdUtil.fastSimpleUUID().substring(0, 8));
        user.setNickname("微信用户");
        user.setStatus(1);
        user.setIsDeleted(0);
        user.setTenantId(tenantId);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);

        userRoleService.saveUserRoles(user.getId(), Collections.singletonList(3L));

        return user;
    }

    private void bindWechatOpenid(User user, WxMaJscode2SessionResult session) {
        try {
            userSocialService.bindOrUpdate(
                    user.getId(),
                    SocialPlatformEnum.WECHAT_MINI,
                    session.getOpenid(),
                    session.getUnionid(),
                    user.getNickname(),
                    user.getAvatar(),
                    session.getSessionKey()
            );
        } catch (Exception e) {
            log.warn("绑定微信 openid 失败，userId={}, openid={}", user.getId(), session.getOpenid(), e);
        }
    }

    private void validateSmsCode(String mobile, String smsCode) {
        String cacheKey = StrUtil.format(RedisConstants.Captcha.SMS_LOGIN_CODE, mobile);
        String cachedCode = (String) redisTemplate.opsForValue().get(cacheKey);

        if (!StrUtil.equals(smsCode, cachedCode)) {
            throw new IllegalArgumentException("验证码错误");
        }

        redisTemplate.delete(cacheKey);
    }

    private AuthenticationToken generateAuthToken(String mobile) {
        SecurityUserDetails userDetails = new SecurityUserDetails(userService.getAuthInfoByMobile(mobile));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        AuthenticationToken authToken = tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authToken;
    }
}
