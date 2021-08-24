package com.facecto.code.token.util;

import com.alibaba.fastjson.JSONObject;
import com.facecto.code.token.entity.TokenUser;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class TokenUtils {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * get TokenUser from principal
     * @return current TokenUser
     */
    public TokenUser getUser(){
        TokenUser user = (TokenUser) SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    /**
     * set user login info, save user permissions roles into redis
     * @param user TokenUser
     * @param userPermissionSet user permissions set
     * @param userRolesSet user roles set
     */
    public void setLoginInfo(TokenUser user, Set<String> userPermissionSet, Set<String> userRolesSet) {
        String s1 = JSONObject.toJSONString(userPermissionSet);
        String s2 = JSONObject.toJSONString(userRolesSet);
        String s3 = JSONObject.toJSONString(user);

        redisTemplate.opsForValue().set("adm-permissions-" + user.getUserId(),s1);
        redisTemplate.opsForValue().set("adm-roles-" + user.getUserId(),s2);
        redisTemplate.opsForValue().set("adm-user-" + user.getUserId(),s3);
    }
}
