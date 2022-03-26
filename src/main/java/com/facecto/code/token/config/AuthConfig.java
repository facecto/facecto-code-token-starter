package com.facecto.code.token.config;

import com.facecto.code.token.auth.AuthRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.2 (2021/02/01)
 */
@Configuration
public class AuthConfig {

    @Autowired
    TokenConfig.AuthFilter authFilter;

    @Bean("securityManager")
    public SecurityManager securityManager(AuthRealm authRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authRealm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

//    @Bean("shiroFilter")
//    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
//        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
//        shiroFilter.setSecurityManager(securityManager);
//        Map<String, Filter> filters = new HashMap<>();
//        filters.put("code-auth", new AuthFilter());
//        shiroFilter.setFilters(filters);
//        Map<String, String> filterMap = new LinkedHashMap<>();
//        for (String a: authFilter.getAnnos()) {
//            filterMap.put(a,"anon");
//        }
//        for(String b: authFilter.getAuths()){
//            filterMap.put(b,"code-auth");
//        }
//        shiroFilter.setFilterChainDefinitionMap(filterMap);
//        return shiroFilter;
//    }

    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
