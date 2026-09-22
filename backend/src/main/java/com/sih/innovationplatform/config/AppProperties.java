package com.sih.innovationplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Ai ai = new Ai();
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {
        private String secret = "dev-secret-change-me-please-32-characters-minimum";
        private long expirationMs = 86400000;
    }

    @Getter
    @Setter
    public static class Ai {
        private String url = "http://localhost:8000";
        private boolean enabled = true;
        private long timeoutMs = 8000;
    }

    @Getter
    @Setter
    public static class Cors {
        private String origin = "http://localhost:5173";
    }
}