package com.facecto.code.token;

import com.alibaba.fastjson.JSON;
import com.facecto.code.base.CodeException;
import com.facecto.code.token.config.TokenConfig;
import com.facecto.code.token.entity.Token;
import com.facecto.code.token.entity.TokenUser;
import com.facecto.code.token.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;


import java.util.Set;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class AuthRealm extends AuthorizingRealm {

    @Autowired(required = true)
    JwtUtils jwtUtils;

    @Autowired(required = true)
    RedisTemplate redisTemplate;

    @Autowired
    private TokenConfig tokenConfig;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof AuthToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        TokenUser user = (TokenUser)principals.getPrimaryPrincipal();
        Integer userId = user.getUserId();

        String permissionString = redisTemplate.opsForValue().get("adm-permissions-" + userId).toString();
        String roleString = redisTemplate.opsForValue().get("adm-roles-" + userId).toString();

        Set<String> permsSet = JSON.parseObject(permissionString, Set.class);
        Set<String> roleSet = JSON.parseObject(roleString, Set.class);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        info.setRoles(roleSet);
        if(SecurityUtils.getSubject().getPrincipal()==null){
            throw new CodeException("需要登录后访问!", HttpStatus.UNAUTHORIZED.value());
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();

        if (StringUtils.isBlank(accessToken)) {
            throw new CodeException("需要登录后访问!!", HttpStatus.UNAUTHORIZED.value());
        }

        Claims claims = null;
        try {
            claims = jwtUtils.getClaimByToken(accessToken);
        } catch (Exception e) {
            throw new CodeException("授权信息错误，请重新登录!", HttpStatus.UNAUTHORIZED.value());
        }

        if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
            throw new CodeException("登录信息失效，请重新登录!!", HttpStatus.UNAUTHORIZED.value());
        }
        String userId = jwtUtils.getUserIdByClaim(accessToken);
        Token redisToken;
        try {
            String key = tokenConfig.getTokenKey() +"-" + userId;
            String tokenString = redisTemplate.opsForValue().get(key).toString();

            redisToken = JSON.parseObject(tokenString,Token.class);
        } catch (Exception e) {
            throw new CodeException("登录信息失效，请重新登录!!!", HttpStatus.UNAUTHORIZED.value());
        }

        if (redisToken != null && redisToken.getToken().equals(accessToken)) {
            String userString = redisTemplate.opsForValue().get("adm-user-" + userId).toString();
            TokenUser user = JSON.parseObject(userString, TokenUser.class);
            if(user == null){
                throw new CodeException("账号失效，请联系管理员!!", HttpStatus.UNAUTHORIZED.value());
            }
            if(user.getStatus() == 1){
                throw new CodeException("账号失效，请联系管理员!!!", HttpStatus.UNAUTHORIZED.value());
            }
            SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
            return info;
        }

        throw new CodeException("登录信息失效，请重新登录!!!!", HttpStatus.UNAUTHORIZED.value());
    }
}
