package com.facecto.code.token.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
@Data
public class Token implements Serializable {
    private static final long serialVersionUID = -1801118915306343230L;
    private String token;
    private Long expire;
}
