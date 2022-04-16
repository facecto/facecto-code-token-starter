package com.facecto.code.token.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SafeConfig
 *
 * @author Jon So, https://facecto.com, https://github.com/facecto
 * @version v1.1.0 (2022/01/01)
 */
@Configuration
@Component
public class TokenConfig {


    @Bean
    @ConfigurationProperties(prefix = "app.api-token")
    public ApiToken apiToken() {
        return new ApiToken();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.auth-filter")
    public AuthFilter authFilter() {
        return new AuthFilter();
    }

    @Getter
    @Setter
    public static class ApiToken {
        private long expire;
        private String key = "";
        private String secret = "";
    }

    @Getter
    @Setter
    public static class AuthFilter {
        private List<String> annos = new ArrayList<>();
        private List<String> auths = new ArrayList<>();
    }
}
