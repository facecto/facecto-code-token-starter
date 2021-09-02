package com.facecto.code.token.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Data
@Accessors(chain = true)
public class TokenInfo implements Serializable {
    private static final long serialVersionUID = 5118619102212724207L;
    private Integer userId;
    private String appKey;
}
