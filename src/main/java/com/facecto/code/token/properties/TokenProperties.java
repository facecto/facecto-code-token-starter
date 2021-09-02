package com.facecto.code.token.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Configuration
@ConfigurationProperties(prefix = "app.token")
public class TokenProperties {
    private final static String KEY = "adm";
    private final static String SECRET = "3d15d32654bc1af61759a3bacbc0c78a";
    private final static Long EXPIRE = 604800L;
    private String key = KEY;
    private String secret = SECRET;
    private Long expire = EXPIRE;

    public String getKey() {
        return this.key;
    }

    public void setKey(String value) {
        this.key = value;
    }

    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String value) {
        this.secret = value;
    }

    public Long getExpire() {
        return this.expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }
}
