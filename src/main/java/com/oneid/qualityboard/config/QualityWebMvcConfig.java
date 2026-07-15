package com.oneid.qualityboard.config;

import com.oneid.qualityboard.security.QualityScopeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class QualityWebMvcConfig implements WebMvcConfigurer {

    private final QualityScopeInterceptor qualityScopeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(qualityScopeInterceptor)
                .addPathPatterns("/api/v1/quality/**")
                .excludePathPatterns("/api/v1/quality/scopes");
    }
}
