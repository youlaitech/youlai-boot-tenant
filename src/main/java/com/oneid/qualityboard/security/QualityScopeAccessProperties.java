package com.oneid.qualityboard.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Location of the mounted quality scope access rules.
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "quality.scope-access")
public class QualityScopeAccessProperties {

    @NotBlank
    private String file;
}
