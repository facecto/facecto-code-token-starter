package com.facecto.code.token.util;

import com.alibaba.fastjson.JSONObject;
import com.facecto.code.base.CodeException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Component
public class RedisUtils {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * Save object to redis
     *
     * @param fullKey fullKey
     * @param o       object
     */
    public void saveObject(String fullKey, Object o) {
        redisTemplate.opsForValue().set(fullKey, JSONObject.toJSONString(o));
    }

    /**
     * Get object from redis
     *
     * @param fullKey fullKey
     * @return object
     */
    public Object getObject(String fullKey) {
        if (StringUtils.isEmpty(fullKey)) {
            throw new CodeException("The key is empty.");
        }
        try {
            Object o = redisTemplate.opsForValue().get(fullKey);
            return o;
        } catch (Exception ex) {
            throw new CodeException("Can't read object data from redis of key:" + fullKey);
        }
    }

    /**
     * Delete key from redis
     *
     * @param fullKey fullKey
     */
    public void delObject(String fullKey) {
        redisTemplate.delete(fullKey);
    }
}
