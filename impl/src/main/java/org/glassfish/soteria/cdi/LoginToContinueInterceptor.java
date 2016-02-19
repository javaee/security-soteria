package org.glassfish.soteria.cdi;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.notNull;
import static org.glassfish.soteria.Utils.validateRequestMethod;
import static org.glassfish.soteria.servlet.RequestCopier.copy;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.glassfish.soteria.servlet.HttpServletRequestDelegator;
import org.glassfish.soteria.servlet.RequestData;


//@Interceptor
@Priority(PLATFORM_BEFORE + 210)
public class LoginToContinueInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private BeanManager beanManager;
    
    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        
        // If intercepting HttpAuthenticationMechanism#validateRequest
        if (isImplementationOf(invocationContext.getMethod(), validateRequestMethod)) {
            return validateRequest(invocationContext,   
                (HttpServletRequest)invocationContext.getParameters()[0],
                (HttpServletResponse)invocationContext.getParameters()[1],
                (HttpMessageContext)invocationContext.getParameters()[2]);
        }
        
        return invocationContext.proceed();
    }
    
    private AuthStatus validateRequest(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        
        if (isOnOriginalURLAfterAuthenticate(request)) {
            
            CredentialValidationResult result = getSavedAuthentication(request);

            HttpServletRequest wrappedRequest = new HttpServletRequestDelegator(request, getSavedRequest(request));
            
            // TODO: Add wrapped request
            return httpMessageContext.notifyContainerAboutLogin(
                  result.getCallerPrincipal(), 
                  result.getCallerGroups());
            
        }
        
        if (httpMessageContext.isProtected()) {
            // TODO: request.authenticate() is captured by this as well
            // Use an "initial call tracker interceptor"?
        }
        
        // Try to authenticate with the next interceptor or actual authentication mechanism
        AuthStatus authstatus = (AuthStatus) invocationContext.proceed();
        
        if (authstatus == SUCCESS && httpMessageContext.getCallerPrincipal() != null) {
            
           
        }
        
        return authstatus;
    }
    
    private boolean isOnOriginalURLAfterAuthenticate(HttpServletRequest request) {
        
        RequestData savedRequest = getSavedRequest(request);
        CredentialValidationResult credentialValidationResult = getSavedAuthentication(request);
        
        return
            notNull(savedRequest, credentialValidationResult) && 
            savedRequest.matchesRequest(request);
        
    }
    
    
    private static final String ORIGINAL_REQUEST_DATA_SESSION_NAME = "org.glassfish.soteria.original.request";
    private static final String AUTHENTICATION_DATA_SESSION_NAME = "org.glassfish.soteria.authentication";

    public void saveRequest(HttpServletRequest request) {
        request.getSession().setAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME, copy(request));
    }

    public RequestData getSavedRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (RequestData) session.getAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
    }

    public void removeSavedRequest(HttpServletRequest request) {
        request.getSession().removeAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
    }
    
    public void saveAuthentication(HttpServletRequest request, CredentialValidationResult credentialValidationResult) {
        request.getSession().setAttribute(AUTHENTICATION_DATA_SESSION_NAME, credentialValidationResult);
    }

    public CredentialValidationResult getSavedAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (CredentialValidationResult) session.getAttribute(AUTHENTICATION_DATA_SESSION_NAME);
    }

    public void removeSavedAuthentication(HttpServletRequest request) {
        request.getSession().removeAttribute(AUTHENTICATION_DATA_SESSION_NAME);
    }
 
}