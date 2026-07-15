package com.oneid.qualityboard.api;

import com.oneid.qualityboard.security.QualityScopeAccessService;
import com.youlai.boot.framework.security.model.SysUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QualityScopeControllerTest {

    private QualityScopeAccessService qualityScopeAccessService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        qualityScopeAccessService = mock(QualityScopeAccessService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new QualityScopeController(qualityScopeAccessService)).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsOnlyTheAuthenticatedCallersGrantedScopes() throws Exception {
        authenticateAs(10001L, "ROLE_QUALITY_READER");
        when(qualityScopeAccessService.allowedScopes(eq(10001L), eq(Set.of("QUALITY_READER"))))
                .thenReturn(new LinkedHashSet<>(Set.of("brand:1001")));

        mockMvc.perform(get("/api/v1/quality/scopes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data[0].scope").value("brand"))
                .andExpect(jsonPath("$.data[0].scope_id").value("1001"))
                .andExpect(jsonPath("$.data[0].label").value("brand 1001"))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(qualityScopeAccessService).allowedScopes(10001L, Set.of("QUALITY_READER"));
    }

    @Test
    void returnsAnEmptyArrayForAnAuthenticatedCallerWithoutGrants() throws Exception {
        authenticateAs(10002L, "ROLE_QUALITY_READER");
        when(qualityScopeAccessService.allowedScopes(eq(10002L), eq(Set.of("QUALITY_READER"))))
                .thenReturn(Set.of());

        mockMvc.perform(get("/api/v1/quality/scopes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private void authenticateAs(Long userId, String role) {
        SysUserDetails user = new SysUserDetails();
        user.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Set.of(new SimpleGrantedAuthority(role)))
        );
    }
}
