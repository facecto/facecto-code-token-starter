package com.facecto.code.token.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facecto.code.base.CodeException;
import com.facecto.code.token.entity.Token;
import com.facecto.code.token.entity.TokenUser;
import com.facecto.code.token.properties.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
@Slf4j
public class TokenUtils {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private TokenProperties tokenProperties;

    /**
     * get TokenUser from principal
     * @return current TokenUser
     */
    public TokenUser getUser(){
        TokenUser user = (TokenUser) SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    /**
     * create a token full mode.
     * @param user TokenUser.
     * @return token.
     */
    public Token createToken(TokenUser user) {
        String tokenKey = tokenProperties.getKey();
        return generateToken(tokenKey,user, false);
    }

    /**
     * create a token simple mode.
     * @param user TokenUser.
     * @param simpleTokenKey key.
     * @return token.
     */
    public Token createToken(TokenUser user, String simpleTokenKey) {
        return generateToken(simpleTokenKey,user, true);
    }

    /**
     * clean token full mode.
     * @param token token
     * @param user TokenUser
     * @return true or false
     * @throws Exception
     */
    public boolean clearToken(String token, TokenUser user) throws Exception {
        String key = tokenProperties.getKey() +"-" + user.getUserId();
        return destoryToken(key,token,user.getUserId());
    }

    /**
     * clean token simple mode.
     * @param token token
     * @param user TokenUser
     * @param tokenKey tokenKey.
     * @return result
     * @throws Exception
     */
    public boolean clearToken(String token, TokenUser user, String tokenKey) throws Exception {
        return destoryToken(tokenKey,token,user.getUserId());
    }

    /**
     * getClaimByToken
     * @param token token
     * @return claim
     */
    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(tokenProperties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (CodeException e) {
            log.debug("validate is token error ", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * getUserIdByClaim
     * @param token token
     * @return userId
     */
    public Integer getUserIdByClaim(String token) {
        try {
            return Integer.parseInt(getClaimByToken(token).getSubject());
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * check token expired
     *
     * @return true：has expired
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * check token expired.
     * @param token token
     * @return true：has expired
     */
    public boolean isTokenExpired(String token){
        Date expiration = getClaimByToken(token).getExpiration();
        return isTokenExpired(expiration);
    }


    /**
     * create a token
     * @param tokenKey key
     * @param user TokenUser
     * @param hasSimple true or false
     * @return token.
     */
    private Token generateToken(String tokenKey, TokenUser user, boolean hasSimple) {
        Date date1 = new Date();
        Date expireDate = new Date(date1.getTime() + tokenProperties.getExpire() * 1000);

        String tokenString = Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(user.getUserId() + "")
                .setIssuedAt(date1)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, tokenProperties.getSecret())
                .compact();
        Token token = new Token();
        token.setToken(tokenString);
        token.setExpire(tokenProperties.getExpire() * 1000);
        try{
            redisTemplate.opsForValue().set(tokenKey +"-" + user.getUserId(), JSONObject.toJSONString(token));
            if(hasSimple){
                setLoginInfo(user,user.getUserPermissionSet(),user.getUserRolesSet());
            } else {
                setLoginInfo(user);
            }
        }
        catch (Exception e){
            throw new CodeException("Redis server error.", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return token;
    }

    /**
     * set user login info, full mode. save user permissions roles into redis
     * @param user TokenUser
     * @param userPermissionSet user permissions set
     * @param userRolesSet user roles set
     */
    private void setLoginInfo(TokenUser user, Set<String> userPermissionSet, Set<String> userRolesSet) {
        redisTemplate.opsForValue().set(tokenProperties.getKey() + "-permissions-" + user.getUserId(),JSONObject.toJSONString(userPermissionSet));
        redisTemplate.opsForValue().set(tokenProperties.getKey() + "-roles-" + user.getUserId(),JSONObject.toJSONString(userRolesSet));
        redisTemplate.opsForValue().set(tokenProperties.getKey() + "-user-" + user.getUserId(),JSONObject.toJSONString(user));
    }

    /**
     * set user login info, simple mode.
     * If you do not use shiro to verify permissions, you can use simple mode.
     * @param user TokenUser
     */
    private void setLoginInfo(TokenUser user) {
        redisTemplate.opsForValue().set(tokenProperties.getKey() + "-user-" + user.getUserId(),JSONObject.toJSONString(user));
    }

    /**
     * clean token
     * @param tokenKey key
     * @param token token
     * @param userId userId
     * @return boolean
     * @throws Exception
     */
    private boolean destoryToken(String tokenKey, String token, Integer userId) throws Exception {
        String key = tokenKey +"-" + userId;
        String token1= redisTemplate.opsForValue().get(key).toString();
        if(token1!=null){
            Token token2 = JSON.parseObject(token1,Token.class);
            if(token2.getToken().equals(token)){
                redisTemplate.delete(key);
                return true;
            }
        }
        return false;
    }
}
