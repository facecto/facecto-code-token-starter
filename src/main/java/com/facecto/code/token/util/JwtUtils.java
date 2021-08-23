package com.facecto.code.token.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facecto.code.base.CodeException;
import com.facecto.code.token.config.ShiroConfig;
import com.facecto.code.token.entity.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
@Slf4j
@Getter
@Setter
public class JwtUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ShiroConfig shiroConfig;

    /**
     * create a token
     * @param userId
     * @return token
     */
    public Token generateToken(int userId) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + shiroConfig.getExpire() * 1000);

        String tokenString = Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(userId + "")
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, shiroConfig.getSecret())
                .compact();
        Token token = new Token();
        token.setToken(tokenString);
        token.setExpire(shiroConfig.getExpire() * 1000);
        try{
            redisTemplate.opsForValue().set(shiroConfig.getTokenKey() +"-" + userId, JSONObject.toJSONString(token));
        }
        catch (Exception e){
            throw new CodeException("Redis server error.", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
        return token;
    }

    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(shiroConfig.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (CodeException e) {
            log.debug("validate is token error ", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    public String getUserIdByClaim(String token) {

        try {
            return getClaimByToken(token).getSubject();
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            throw new CodeException("Token error!", HttpStatus.SERVICE_UNAVAILABLE.value());
        }
    }

    /**
     * clean token
     * @param token
     * @param userId
     * @return true or false
     * @throws Exception
     */
    public boolean clearToken(String token, Integer userId) throws Exception {

        String key = shiroConfig.getTokenKey() +"-" + userId;
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

    /**
     * check token expired
     *
     * @return true：has expired
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

}