package com.facecto.code.token.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Jon So, https://facecto.com, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Data
@Accessors(chain = true)
public class TokenUser implements Serializable {
    private static final long serialVersionUID = -1250173609480244702L;
    private Integer userId;
    private String userName;
    private String userTel;
    private LocalDateTime userLoginTime;
    private Integer status;
    /**
     * If used simple mode, it's no required.
     */
    private Set<String> userPermissionSet;
    /**
     * If used simple mode, it's no required.
     */
    private Set<String> userRolesSet;
}
