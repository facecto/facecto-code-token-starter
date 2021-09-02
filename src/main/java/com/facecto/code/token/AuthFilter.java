package com.facecto.code.token;

import com.alibaba.fastjson.JSONObject;
import com.facecto.code.base.CodeException;
import com.facecto.code.base.CodeResult;
import com.facecto.code.base.util.HttpContextUtils;
import com.facecto.code.token.properties.TokenProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * For full mode.
 * Note that the shiro function can only be used in the full mode.
 *
 * @author Jon So, https://cto.pub, https://github.com/facecto
 * @version v1.1.0 (2021/08/08)
 */
public class AuthFilter extends AuthenticatingFilter {

    @Autowired
    TokenProperties tokenProperties;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return new AuthToken(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
            httpResponse.setContentType("application/json;charset=UTF-8");
            String json = JSONObject.toJSONString(CodeResult.error(HttpStatus.SC_UNAUTHORIZED, "Need authorization!"));
            httpResponse.getWriter().print(json);
            return false;
        }

        return executeLogin(request, response);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        AuthenticationToken token = this.createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        } else {
            try {
                Subject subject = this.getSubject(request, response);
                subject.login(token);
                return this.onLoginSuccess(token, subject, request, response);
            } catch (AuthenticationException ex) {
                return this.onLoginFailure(token, ex, request, response);
            }
        }
    }


    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
        try {
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            CodeResult result = CodeResult.error(HttpStatus.SC_UNAUTHORIZED, throwable.getMessage());
            String json = JSONObject.toJSONString(result);
            httpResponse.getWriter().print(json);
        } catch (IOException ex) {

        }

        return false;
    }

    private String getRequestToken(HttpServletRequest httpRequest) {
        String token = "";
        try {
            token = httpRequest.getHeader("token");
            if (StringUtils.isEmpty(token)) {
                token = httpRequest.getParameter("token");
            }
            return token;
        } catch (Exception e) {
            throw new CodeException("Token is null!");
        }
    }


}
