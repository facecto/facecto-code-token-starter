package com.facecto.code.token.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facecto.code.base.CodeException;
import com.facecto.code.token.entity.Token;
import com.facecto.code.token.entity.TokenInfo;
import com.facecto.code.token.entity.TokenUser;
import com.facecto.code.token.properties.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    RedisUtils redisUtils;

    @Autowired
    private TokenProperties tokenProperties;

    /**
     * Get tokenUser by principal
     * used shiro
     * @return current TokenUser
     */
    public TokenUser getUser(){
        TokenUser user = (TokenUser) SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    /**
     * Get tokenUser from token from redis
     * no used shiro
     * @param token token
     * @return tokenUser
     */
    public TokenUser getUser(String token){
        TokenInfo tokenInfo = getTokenInfoByClaim(token);
        Object oo = redisUtils.getObject(KeysUtils.getUserKey(tokenInfo.getAppKey(), tokenInfo.getUserId()));
        TokenUser user = JSONObject.parseObject(oo.toString(), TokenUser.class);
        return user;
    }

    /**
     * Get user permission by default key from redis
     * @param token token
     * @return user permission set
     */
    public Set<String> getUserPermission(String token){
        TokenInfo tokenInfo = getTokenInfoByClaim(token);
        Object oo = redisUtils.getObject(KeysUtils.getPermissionKey(tokenInfo.getAppKey(), tokenInfo.getUserId()));
        return JSONObject.parseObject(oo.toString(), Set.class);
    }


    /**
     * Get user role set by default key from redis
     * @param token token
     * @return user role set
     */
    public Set<String> getUserRole(String token){
        TokenInfo tokenInfo = getTokenInfoByClaim(token);
        Object oo = redisUtils.getObject(KeysUtils.getRolesKey(tokenInfo.getAppKey(), tokenInfo.getUserId()));
        return JSONObject.parseObject(oo.toString(), Set.class);
    }

    /**
     * Get token from redis by param key from redis
     * @param baseKey baseKey
     * @param user user
     * @return token object
     */
    public Token getToken(String baseKey, TokenUser user){
        Object o = redisUtils.getObject(KeysUtils.getTokenKey(baseKey,user));
        return JSON.parseObject(o.toString(), Token.class);
    }

    /**
     * Get token from redis by default key from redis
     * @param user user
     * @return token
     */
    public Token getToken(TokenUser user){
        return getToken(tokenProperties.getKey(),user);
    }

    /**
     * Create a token by default key and save in redis
     * @param user TokenUser.
     * @return token.
     */
    public Token createToken(TokenUser user) {
        String baseKey = tokenProperties.getKey();
        return generateToken(baseKey, user, false);
    }

    /**
     * Create a token by param key and save in redis
     * @param user TokenUser.
     * @param baseKey key.
     * @return token.
     */
    public Token createToken(TokenUser user, String baseKey) {
        return generateToken(baseKey, user, true);
    }


    /**
     * Clean token by default key (delete redis key)
     * @param token token
     * @param user user
     * @return result
     * @throws Exception
     */
    public boolean cleanToken(String token, TokenUser user) throws Exception {
        String baseKey = tokenProperties.getKey();
        return destroyToken(token,user,baseKey);
    }

    /**
     * Clean token by param key (delete redis key)
     * @param token token
     * @param user user
     * @param baseKey baseKey
     * @return result
     * @throws Exception
     */
    public boolean cleanToken(String token, TokenUser user, String baseKey) throws Exception {
        return destroyToken(token,user,baseKey);
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
            log.debug("Token error!", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * Get userId by claim
     * @param token token
     * @return userId
     */
    public TokenInfo getTokenInfoByClaim(String token) {
        try {
            String[] strings = getClaimByToken(token).getSubject().split("\\|");
            TokenInfo tokenInfo = new TokenInfo()
                    .setUserId(Integer.parseInt(strings[1]))
                    .setAppKey(strings[0]);
            return tokenInfo;
        } catch (Exception e) {
            log.debug("Token error!", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * Check token expired
     *
     * @return true：expired
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * Check token expired.
     * @param token token
     * @return true：expired
     */
    public boolean isTokenExpired(String token){
        Date expiration = getClaimByToken(token).getExpiration();
        return isTokenExpired(expiration);
    }


    /**
     * Create a token and save in redis
     * @param baseKey baseKey
     * @param user TokenUser
     * @param hasSimple true or false
     * @return token.
     */
    private Token generateToken(String baseKey, TokenUser user, boolean hasSimple) {
        Date date1 = new Date();
        Date expireDate = new Date(date1.getTime() + tokenProperties.getExpire() * 1000);

        String tokenString = Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(baseKey +"|" + user.getUserId())
                .setIssuedAt(date1)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, tokenProperties.getSecret())
                .compact();
        Token token = new Token();
        token.setToken(tokenString);
        token.setExpire(tokenProperties.getExpire() * 1000);
        try{
            saveToken(baseKey,user,tokenString);
            if(!hasSimple){
                saveLoginInfo(baseKey,user,user.getUserPermissionSet(),user.getUserRolesSet());
            } else {
                saveLoginInfo(baseKey,user);
            }
        }
        catch (Exception e){
            throw new CodeException("Redis server error.", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return token;
    }

    /**
     * Save user login info: user, permissions, roles into redis
     * @param baseKey baseKey
     * @param user user
     * @param userPermissionSet permission set
     * @param userRolesSet role set
     */
    private void saveLoginInfo(String baseKey, TokenUser user, Set<String> userPermissionSet, Set<String> userRolesSet) {
        redisUtils.saveObject(KeysUtils.getUserKey(baseKey,user),user);
        redisUtils.saveObject(KeysUtils.getPermissionKey(baseKey,user),userPermissionSet);
        redisUtils.saveObject(KeysUtils.getRolesKey(baseKey,user),userRolesSet);
    }


    /**
     * Save user login info: user into redis
     * @param baseKey baseKey
     * @param user user
     */
    private void saveLoginInfo(String baseKey, TokenUser user) {
        redisUtils.saveObject(KeysUtils.getUserKey(baseKey,user),user);
    }

    /**
     * Save token to redis
     * @param baseKey baseKey
     * @param user tokenUser
     * @param token token
     */
    private void saveToken(String baseKey, TokenUser user, String token){
        redisUtils.saveObject(KeysUtils.getTokenKey(baseKey,user),token);
    }

    /**
     * Destroy token (delete redis key)
     * @param token tokenString
     * @param user user
     * @param baseKey basekey
     * @return true or false
     */
    private boolean destroyToken(String token, TokenUser user, String baseKey) {
        Token token1 = getToken(baseKey,user);
        if(token1.getToken().equals(token)){
            redisUtils.delObject(KeysUtils.getTokenKey(baseKey,user));
            return true;
        }
        return false;
    }

}
