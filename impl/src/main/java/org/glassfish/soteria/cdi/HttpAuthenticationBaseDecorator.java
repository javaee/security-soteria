package org.glassfish.soteria.cdi;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Decorator
@Priority(PLATFORM_BEFORE + 200)
public abstract class HttpAuthenticationBaseDecorator implements HttpAuthenticationMechanism, Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @Delegate
    HttpAuthenticationMechanism delegateMechanism;

    @Override
    public AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
        return delegateMechanism.validateRequest(request, response, httpMessageContext);
    }
    
    @Override
    public AuthStatus secureResponse(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
        return delegateMechanism.secureResponse(request, response, httpMessageContext);
    }
    
    @Override
    public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        delegateMechanism.cleanSubject(request, response, httpMessageContext);
    }

}