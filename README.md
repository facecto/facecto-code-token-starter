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

## Step 3 : No more step. enjoy it.


# About facecto.com
https://facecto.com

