package com.facecto.code.token.util;

import com.facecto.code.token.entity.TokenUser;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class TokenUtils {
    public TokenUser getUser(){
        TokenUser user = (TokenUser) SecurityUtils.getSubject().getPrincipal();
        return user;
    }
}
