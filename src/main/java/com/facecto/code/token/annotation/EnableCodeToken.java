package com.facecto.code.token.annotation;

import com.facecto.code.token.config.TokenAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation EnableCodeSafe
 * @author Jon So, https://cto.pub, https://facecto.com, https://github.com/facecto
 * @version v1.1.2 (2022/02/01)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(TokenAutoConfiguration.class)
@Documented
@Inherited
public @interface EnableCodeToken {
}
