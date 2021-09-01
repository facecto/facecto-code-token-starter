package com.facecto.code.token.util;

import com.facecto.code.token.entity.TokenUser;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
public class KeysUtils {
    /**
     * get tokenKey
     * @param baseKey baseKey
     * @param user user
     * @return
     */
    public static String getTokenKey(String baseKey, TokenUser user){
        return baseKey + "-user-" + user.getUserId() +"-token";
    }

    /**
     * get permissionKey
     * @param baseKey baseKey
     * @param user user
     * @return
     */
    public static String getPermissionKey(String baseKey, TokenUser user){
        return baseKey + "-user-" + user.getUserId() +"-permissions";
    }

    /**
     * get roleKey
     * @param baseKey baseKey
     * @param user user
     * @return roleKey
     */
    public static String getRolesKey(String baseKey, TokenUser user){
        return baseKey + "-user-" + user.getUserId() +"-roles";
    }

    /**
     * get userKey
     * @param baseKey baseKey
     * @param user user
     * @return userkey
     */
    public static String getUserKey(String baseKey, TokenUser user){
        return baseKey + "-user-" + user.getUserId();
    }

    /**
     * get tokenKey
     * @param baseKey baseKey
     * @param userId userId
     * @return tokenKey
     */
    public static String getTokenKey(String baseKey, Integer userId){
        return baseKey + "-user-" + userId +"-token";
    }

    /**
     * get permissionKey
     * @param baseKey baseKey
     * @param userId userId
     * @return permissionKey
     */
    public static String getPermissionKey(String baseKey, Integer userId){
        return baseKey + "-user-" + userId +"-permissions";
    }

    /**
     * get roleKey
     * @param baseKey baseKey
     * @param userId userId
     * @return roleKey
     */
    public static String getRolesKey(String baseKey, Integer userId){
        return baseKey + "-user-" + userId +"-roles";
    }

    /**
     * get userKey
     * @param baseKey baseKey
     * @param userId userId
     * @return userKey
     */
    public static String getUserKey(String baseKey, Integer userId){
        return baseKey + "-user-" + userId;
    }
}
