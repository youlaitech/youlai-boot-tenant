package com.youlai.boot.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 限流配置
 *
 * @author Ray.Hao
 * @since 4.3.1
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private int defaultLimit = 5;

    private Duration defaultWindow = Duration.ofSeconds(60);

    private Ip ip = new Ip();

    @Data
    public static class Ip {

        private boolean enabled = true;

        private int limit = 1000;

        private Duration window = Duration.ofSeconds(60);

    }

}