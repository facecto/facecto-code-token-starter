# About facecto-code-token-starter
A token base library, based on shiro to achieve login verification.

# Quick Start
## Step 1: setting the pom.xml add dependency 
```
<dependency>
  <groupId>com.facecto.code</groupId>
  <artifactId>facecto-code-token-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```
## Setp 2: setting application.yaml
```
app:
  safe:
    token-key: String format. the key in redis.
    token-name: true|false. the token name in http head.
    secret: String format. the token secret. example: "3d15d32654bc1af61759a3bacbc0c78a"
    expire: Integer format. the token expire time (seconds).    
```

## Step 3：create ShiroConfig.java in your project
Note that the shiro function can only be used in the full mode.
```
import com.facecto.code.token.AuthFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.context.annotation.Bean;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
@Configuration
public class ShiroConfig {
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);
        Map<String, Filter> filters = new HashMap<>();
        filters.put("oauth2", new AuthFilter());
        shiroFilter.setFilters(filters);
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/login", "anon");
        filterMap.put("/**", "oauth2");
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }
}
```

## Step 4 : add annotation
```
@ComponentScans({
        @ComponentScan(basePackages = {
                "com.facecto.code"
        })
})
@EnableConfigurationProperties(TokenProperties.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
```

## Step 5 : No more step. enjoy it.

## Notice 1: redis need.
This component uses redis to access related authorization information. Redis must be introduced in the SpringBoot project.
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
## Notice 2: app.token.key and simple mode.
The default value of app.token.key is "adm-token".
simple mode: If you don't use shiro's authorization and verification functions, you can use the simple mode.
Simple mode allows you to use multiple custom keys in a project.

### simple mode example.
```
Token token = generateTokenSimple("app-1", userId);

```

# About facecto.com
https://facecto.com

