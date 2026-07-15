package com.oneid.qualityboard.config;

import com.oneid.qualityboard.security.QualityScopeAccessProperties;
import com.oneid.qualityboard.security.QualityScopeAccessService;
import com.oneid.qualityboard.security.QualityScopeInterceptor;
import com.youlai.boot.framework.security.model.SysUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QualityWebMvcConfigTest {

    @TempDir
    Path temporaryDirectory;

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private QualityTestController controller;

    @BeforeEach
    void setUp() throws IOException {
        TestRules.file = Files.writeString(temporaryDirectory.resolve("quality-scope-access.yml"), """
                user_ids:
                  \"10001\": [\"brand:1001\"]
                roles: {}
                """).toString();
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestWebMvcConfiguration.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        controller = context.getBean(QualityTestController.class);
        authenticateAs(10001L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    @Test
    void interceptsProtectedQualityPathsBeforeTheHandler() throws Exception {
        mockMvc.perform(get("/api/v1/quality/metrics"))
                .andExpect(status().isBadRequest());

        assertThat(controller.metricsHandled).isFalse();
    }

    @Test
    void excludesTheScopesEndpointFromScopeParameterChecks() throws Exception {
        mockMvc.perform(get("/api/v1/quality/scopes"))
                .andExpect(status().isOk())
                .andExpect(content().string("scopes"));

        assertThat(controller.scopesHandled).isTrue();
    }

    private void authenticateAs(Long userId) {
        SysUserDetails user = new SysUserDetails();
        user.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    }

    @Configuration
    @EnableWebMvc
    @Import(QualityWebMvcConfig.class)
    static class TestWebMvcConfiguration {

        @Bean
        QualityScopeAccessProperties qualityScopeAccessProperties() {
            QualityScopeAccessProperties properties = new QualityScopeAccessProperties();
            properties.setFile(TestRules.file);
            return properties;
        }

        @Bean
        QualityScopeAccessService qualityScopeAccessService(QualityScopeAccessProperties properties) {
            return new QualityScopeAccessService(properties);
        }

        @Bean
        QualityScopeInterceptor qualityScopeInterceptor(QualityScopeAccessService service) {
            return new QualityScopeInterceptor(service);
        }

        @Bean
        QualityTestController qualityTestController() {
            return new QualityTestController();
        }
    }

    @Controller
    static class QualityTestController {

        private boolean metricsHandled;
        private boolean scopesHandled;

        @GetMapping("/api/v1/quality/metrics")
        @ResponseBody
        String metrics() {
            metricsHandled = true;
            return "metrics";
        }

        @GetMapping("/api/v1/quality/scopes")
        @ResponseBody
        String scopes() {
            scopesHandled = true;
            return "scopes";
        }
    }

    static class TestRules {

        private static String file;
    }
}
