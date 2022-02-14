package com.facecto.code.token.auth;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * For full mode.
 * Note that the shiro function can only be used in the full mode.
 *
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
public class AuthToken implements AuthenticationToken {
    private final String token;

    public AuthToken(String token) {
        this.token = token;
    }

    @Override
    public String getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
