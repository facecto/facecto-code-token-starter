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
     * Get tokenUser from principal
     * use shiro
     * @return current TokenUser
     */
    public TokenUser getUser(){
        TokenUser user = (TokenUser) SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    /**
     * Get tokenUser from token
     * no use shiro
     * @param tokenKey
     * @param token
     * @return
     */
    public TokenUser getUser(String tokenKey, String token){
        Integer userId = getUserIdByClaim(token);
        Object o = redisTemplate.opsForValue().get(tokenKey + "-user-" + userId);
        TokenUser user = JSONObject.parseObject(o.toString(), TokenUser.class);
        return user;
    }

    /**
     * Create a token full mode.
     * the key in config.yaml
     * @param user TokenUser.
     * @return token.
     */
    public Token createToken(TokenUser user) {
        String tokenKey = tokenProperties.getKey();
        return generateToken(tokenKey, user, false);
    }

    /**
     * Create a token simple mode.
     * used param key
     * @param user TokenUser.
     * @param tokenKey key.
     * @return token.
     */
    public Token createToken(TokenUser user, String tokenKey) {
        return generateToken(tokenKey, user, true);
    }


    /**
     * Clean token
     * the key in config.yaml
     * @param token token
     * @param user user
     * @return result
     * @throws Exception
     */
    public boolean cleanToken(String token, TokenUser user) throws Exception {
        String key = tokenProperties.getKey();
        return destoryToken(token,user.getUserId(),key);
    }

    /**
     * Clean token
     * used param key
     * @param token token
     * @param user user
     * @param key key
     * @return result
     * @throws Exception
     */
    public boolean cleanToken(String token, TokenUser user, String key) throws Exception {
        return destoryToken(token,user.getUserId(),key);
    }

    /**
     * Get claim by token
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
     * get userId by claim
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
     * Check token expired
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
     * Create a token
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
            saveTokenInfo(tokenKey,user,tokenString);
            if(!hasSimple){
                saveLoginInfo(tokenKey,user,user.getUserPermissionSet(),user.getUserRolesSet());
            } else {
                saveLoginInfo(tokenKey,user);
            }
        }
        catch (Exception e){
            throw new CodeException("Redis server error.", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return token;
    }

    /**
     * save token to redis
     * @param key key
     * @param user tokenUser
     * @param token token
     */
    private void saveTokenInfo(String key,TokenUser user, String token){
        redisTemplate.opsForValue().set(key +"-" + user.getUserId(), JSONObject.toJSONString(token));
    }

    /**
     * set user login info, full mode. save user permissions roles into redis
     * @param user TokenUser
     * @param userPermissionSet user permissions set
     * @param userRolesSet user roles set
     */
    private void saveLoginInfo(String key, TokenUser user, Set<String> userPermissionSet, Set<String> userRolesSet) {
        redisTemplate.opsForValue().set(key + "-permissions-" + user.getUserId(),JSONObject.toJSONString(userPermissionSet));
        redisTemplate.opsForValue().set(key + "-roles-" + user.getUserId(),JSONObject.toJSONString(userRolesSet));
        redisTemplate.opsForValue().set(key + "-user-" + user.getUserId(),JSONObject.toJSONString(user));
    }

    /**
     * set user login info, simple mode.
     * If you do not use shiro to verify permissions, you can use simple mode.
     * @param user TokenUser
     */
    private void saveLoginInfo(String key, TokenUser user) {
        redisTemplate.opsForValue().set(key + "-user-" + user.getUserId(),JSONObject.toJSONString(user));
    }

    /**
     * delete token
     * @param token  tokenString
     * @param userId userId
     * @param tokenKey tokenKey
     * @return boolean
     * @throws Exception
     */
    private boolean destoryToken(String token, Integer userId,String tokenKey) throws Exception {
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
