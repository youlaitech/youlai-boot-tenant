package com.oneid.qualityboard.api;

import com.oneid.qualityboard.security.QualityScopeAccessService;
import com.youlai.boot.framework.captcha.service.CaptchaService;
import com.youlai.boot.framework.security.config.SecurityConfig;
import com.youlai.boot.framework.security.config.SecurityProperties;
import com.youlai.boot.framework.security.model.SysUserDetails;
import com.youlai.boot.framework.security.service.SysUserDetailsService;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.auth.controller.AuthController;
import com.youlai.boot.system.service.ConfigService;
import com.youlai.boot.system.service.UserService;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QualityScopeSecurityIntegrationTest {

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private TokenManager tokenManager;
    private QualityScopeAccessService qualityScopeAccessService;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SecurityTestConfiguration.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
        tokenManager = context.getBean(TokenManager.class);
        qualityScopeAccessService = context.getBean(QualityScopeAccessService.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void rejectsAnAnonymousScopesRequest() throws Exception {
        mockMvc.perform(get("/api/v1/quality/scopes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void letsAValidBearerTokenReachTheScopesEndpoint() throws Exception {
        SysUserDetails user = new SysUserDetails();
        user.setUserId(10001L);
        when(tokenManager.validateToken("valid-token")).thenReturn(true);
        when(tokenManager.parseToken("valid-token")).thenReturn(
                new UsernamePasswordAuthenticationToken(user, null, Set.of(new SimpleGrantedAuthority("ROLE_QUALITY_READER")))
        );
        when(qualityScopeAccessService.allowedScopes(eq(10001L), eq(Set.of("QUALITY_READER"))))
                .thenReturn(Set.of("brand:1001"));

        mockMvc.perform(get("/api/v1/quality/scopes").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].scope").value("brand"));
    }

    @Test
    void letsTheDashboardLoginRequestReachItsControllerWithoutCaptcha() throws Exception {
        mockMvc.perform(post("/api/v1/quality-auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }

    @Configuration
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class SecurityTestConfiguration {

        @Bean
        QualityScopeAccessService qualityScopeAccessService() {
            return mock(QualityScopeAccessService.class);
        }

        @Bean
        QualityScopeController qualityScopeController(QualityScopeAccessService service) {
            return new QualityScopeController(service);
        }

        @Bean
        QualityDashboardAuthController qualityDashboardAuthController(AuthController authController) {
            return new QualityDashboardAuthController(authController);
        }

        @Bean
        AuthController authController() {
            return mock(AuthController.class);
        }

        @Bean
        @SuppressWarnings({"unchecked", "rawtypes"})
        RedisTemplate<String, Object> redisTemplate() {
            RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
            ValueOperations<String, Object> values = mock(ValueOperations.class);
            when(redisTemplate.opsForValue()).thenReturn(values);
            when(values.increment(anyString())).thenReturn(1L);
            return redisTemplate;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        TokenManager tokenManager() {
            return mock(TokenManager.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        SysUserDetailsService sysUserDetailsService() {
            return mock(SysUserDetailsService.class);
        }

        @Bean
        CaptchaService captchaService() {
            return mock(CaptchaService.class);
        }

        @Bean
        ConfigService configService() {
            return mock(ConfigService.class);
        }

        @Bean
        SecurityProperties securityProperties() {
            SecurityProperties properties = new SecurityProperties();
            properties.setIgnoreUrls(new String[]{"/api/v1/auth/login/**"});
            properties.setUnsecuredUrls(new String[]{"/doc.html"});
            return properties;
        }
    }
}
