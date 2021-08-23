package com.facecto.code.token.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Configuration
@ConfigurationProperties(prefix = "app.token")
public class ShiroProperties {
    private final static String TOKEN_KEY = "adm-token";
    private final static String SECRET = "3d15d32654bc1af61759a3bacbc0c78a";
    private final static String TOKEN_NAME = "token";
    private final static Long EXPIRE = 604800L;
    private String tokenKey = TOKEN_KEY;
    private String secret = SECRET;
    private String tokenName = TOKEN_NAME;
    private Long expire = EXPIRE;

    public String getTokenKey() {return tokenKey;}
    public void setTokenKey(String value) {this.tokenKey = value;}

    public String getSecret() { return SECRET;}
    public void setSecret(String value) {this.secret = value;}

    public String getTokenName() {return TOKEN_NAME;}
    public void setTokenName(String value) {this.tokenName = value;}

    public Long getExpire() {return expire;}
    public void setExpire(Long value) {this.expire = value;}
}
