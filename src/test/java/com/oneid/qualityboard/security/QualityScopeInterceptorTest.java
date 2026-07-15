package com.oneid.qualityboard.security;

import com.youlai.boot.framework.security.model.SysUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class QualityScopeInterceptorTest {

    @TempDir
    Path temporaryDirectory;

    private QualityScopeInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        Path config = Files.writeString(temporaryDirectory.resolve("quality-scope-access.yml"), """
                user_ids:
                  \"10001\": [\"brand:1001\"]
                roles: {}
                """);
        QualityScopeAccessProperties properties = new QualityScopeAccessProperties();
        properties.setFile(config.toString());
        interceptor = new QualityScopeInterceptor(new QualityScopeAccessService(properties));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsAnonymousRequests() throws Exception {
        MockHttpServletResponse response = invoke(requestWithScope("brand", "1001"));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsRequestsWithoutBothScopeParameters() throws Exception {
        authenticateAs(10001L);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/quality/metrics");
        request.setParameter("scope", "brand");

        MockHttpServletResponse response = invoke(request);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @ParameterizedTest
    @MethodSource("malformedScopeParameters")
    void rejectsMalformedScopeParameters(String scope, String scopeId) throws Exception {
        authenticateAs(10001L);

        MockHttpServletResponse response = invoke(requestWithScope(scope, scopeId));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void rejectsARequestedScopeThatIsNotGranted() throws Exception {
        authenticateAs(10001L);

        MockHttpServletResponse response = invoke(requestWithScope("brand", "1002"));

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void permitsAnExactGrantedScope() throws Exception {
        authenticateAs(10001L);
        MockHttpServletRequest request = requestWithScope("brand", "1001");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
    }

    private MockHttpServletResponse invoke(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        return response;
    }

    private MockHttpServletRequest requestWithScope(String scope, String scopeId) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/quality/metrics");
        request.setParameter("scope", scope);
        request.setParameter("scope_id", scopeId);
        return request;
    }

    private static Stream<String[]> malformedScopeParameters() {
        return Stream.of(
                new String[]{"brand:region", "1001"},
                new String[]{"", "1001"},
                new String[]{" brand", "1001"}
        );
    }

    private void authenticateAs(Long userId) {
        SysUserDetails user = new SysUserDetails();
        user.setUserId(userId);
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_QUALITY_READER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }
}
