package com.facecto.code.token.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
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
}
