package com.oneid.qualityboard.api;

import com.youlai.boot.auth.controller.AuthController;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QualityDashboardAuthControllerTest {

    @Test
    void logsInWithoutCaptchaFields() throws Exception {
        AuthController authController = mock(AuthController.class);
        @SuppressWarnings("unchecked")
        com.youlai.boot.common.result.Result<?> loginResult = com.youlai.boot.common.result.Result.success(AuthenticationToken.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .build());
        doReturn(loginResult).when(authController).login(any(), any());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new QualityDashboardAuthController(authController)).build();

        mockMvc.perform(post("/api/v1/quality-auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\",\"tenantId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }
}
