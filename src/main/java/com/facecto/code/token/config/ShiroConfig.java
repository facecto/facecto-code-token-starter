package com.facecto.code.token.config;

import com.facecto.code.token.properties.ShiroProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class ShiroConfig {
    @Autowired
    ShiroProperties shiroProperties;

    public String getTokenKey() {
        return shiroProperties.getTokenKey();
    }

    public String getSecret(){
        return shiroProperties.getSecret();
    }

    public String getTokenName() {
        return shiroProperties.getTokenName();
    }

    public Long getExpire() {
        return shiroProperties.getExpire();
    }

}
