package org.glassfish.soteria.cdi;

import static java.lang.Boolean.TRUE;
import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.validateRequestMethod;

import java.io.Serializable;
import java.security.Principal;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;

@Interceptor
@AutoApplySession
@Priority(PLATFORM_BEFORE + 200)
public class AutoApplySessionInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        
        if (isImplementationOf(invocationContext.getMethod(), validateRequestMethod)) {
            
            HttpMessageContext httpMessageContext = (HttpMessageContext)invocationContext.getParameters()[2];
            Principal userPrincipal = httpMessageContext.getRequest().getUserPrincipal();
            
            if (userPrincipal != null) {
                
                httpMessageContext.getHandler().handle(new Callback[] { 
                    new CallerPrincipalCallback(httpMessageContext.getClientSubject(), userPrincipal) }
                );
                         
                return SUCCESS;
            }
            
            Object outcome = invocationContext.proceed();
            
            if (SUCCESS.equals(outcome)) {
                httpMessageContext.getMessageInfo().getMap().put("javax.servlet.http.registerSession", TRUE.toString());
            }
            
            return outcome;
        }
        
        return invocationContext.proceed();
    }
    
}