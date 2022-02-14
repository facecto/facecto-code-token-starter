package com.facecto.code.token.config;

import com.facecto.code.token.properties.TokenProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SafeConfig
 * @author Jon So, https://cto.pub, https://facecto.com, https://github.com/facecto
 * @version v1.1.0 (2022/01/01)
 */
@Component
public class TokenConfig {
    @Autowired
    TokenProperties tokenProperties;

    private String key ;
    private String secret;
    private Long expire ;

    public String getKey() {
        return tokenProperties.getKey();
    }

    public String getSecret() {
        return tokenProperties.getSecret();
    }
    public Long getExpire() {
        return tokenProperties.getExpire();
    }

}
