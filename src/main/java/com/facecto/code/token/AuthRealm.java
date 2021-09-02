package com.facecto.code.token;

import com.alibaba.fastjson.JSON;
import com.facecto.code.base.CodeException;
import com.facecto.code.token.entity.Token;
import com.facecto.code.token.entity.TokenInfo;
import com.facecto.code.token.entity.TokenUser;
import com.facecto.code.token.properties.TokenProperties;
import com.facecto.code.token.util.KeysUtils;
import com.facecto.code.token.util.RedisUtils;
import com.facecto.code.token.util.TokenUtils;
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
 * For full mode.
 * Note that the shiro function can only be used in the full mode.
 *
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class AuthRealm extends AuthorizingRealm {
    @Autowired
    TokenUtils tokenUtils;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    TokenProperties tokenProperties;
    @Autowired
    RedisUtils redisUtils;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof AuthToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        TokenUser user = (TokenUser) principals.getPrimaryPrincipal();
        Integer userId = user.getUserId();
        Set<String> permsSet = JSON.parseObject(redisUtils
                .getObject(KeysUtils.getPermissionKey(tokenProperties.getKey(), userId)).toString(), Set.class);
        Set<String> roleSet = JSON.parseObject(redisUtils
                .getObject(KeysUtils.getRolesKey(tokenProperties.getKey(), userId)).toString(), Set.class);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        info.setRoles(roleSet);
        if (SecurityUtils.getSubject().getPrincipal() == null) {
            throw new CodeException("Authorization required!", HttpStatus.UNAUTHORIZED.value());
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();
        if (StringUtils.isBlank(accessToken)) {
            throw new CodeException("Authorization required!!", HttpStatus.UNAUTHORIZED.value());
        }
        Claims claims = null;
        try {
            claims = tokenUtils.getClaimByToken(accessToken);
            if (claims == null || tokenUtils.isTokenExpired(claims.getExpiration())) {
                throw new CodeException("The authorization is invalid, please login again!", HttpStatus.UNAUTHORIZED.value());
            }
            TokenInfo tokenInfo = tokenUtils.getTokenInfoByClaim(accessToken);
            if (!tokenInfo.getAppKey().equals(tokenProperties.getKey())) {
                throw new CodeException("The token is invalid, check baseKey!", HttpStatus.UNAUTHORIZED.value());
            }
            Token redisToken = JSON.parseObject(redisUtils.getObject(KeysUtils.getTokenKey(tokenInfo.getAppKey(), tokenInfo.getUserId())).toString(), Token.class);
            if (redisToken != null && redisToken.getToken().equals(accessToken)) {
                TokenUser user = JSON.parseObject(redisUtils.getObject(KeysUtils.getUserKey(tokenInfo.getAppKey(), tokenInfo.getUserId())).toString(), TokenUser.class);
                if (user == null) {
                    throw new CodeException("The account is invalid!", HttpStatus.UNAUTHORIZED.value());
                }
                if (user.getStatus() == 1) {
                    throw new CodeException("The account is invalid!!", HttpStatus.UNAUTHORIZED.value());
                }
                return new SimpleAuthenticationInfo(user, accessToken, getName());
            }
        } catch (Exception e) {
            throw new CodeException("The authorization information is wrong!", HttpStatus.UNAUTHORIZED.value());
        }

        throw new CodeException("The authorization is invalid, please login again!!!", HttpStatus.UNAUTHORIZED.value());
    }
}
