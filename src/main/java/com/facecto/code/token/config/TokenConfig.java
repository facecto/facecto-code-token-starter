package com.facecto.code.token.config;

import com.facecto.code.token.properties.TokenProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class TokenConfig {
    @Autowired
    TokenProperties tokenProperties;

    public String getTokenKey() {
        return tokenProperties.getTokenKey();
    }

    public String getSecret(){
        return tokenProperties.getSecret();
    }

    public String getTokenName() {
        return tokenProperties.getTokenName();
    }

    public Long getExpire() {
        return tokenProperties.getExpire();
    }

}
