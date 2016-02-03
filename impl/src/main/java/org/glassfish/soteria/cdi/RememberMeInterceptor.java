package org.glassfish.soteria.cdi;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static org.glassfish.soteria.Utils.cleanSubjectMethod;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.validateRequestMethod;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;
import static org.glassfish.soteria.servlet.CookieHandler.getCookie;
import static org.glassfish.soteria.servlet.CookieHandler.removeCookie;
import static org.glassfish.soteria.servlet.CookieHandler.saveCookie;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Priority;
import javax.el.ELProcessor;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.RememberMe;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.RememberMeIdentityStore;
import javax.security.identitystore.credential.RememberMeCredential;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Interceptor
@RememberMe
@Priority(PLATFORM_BEFORE + 210)
public class RememberMeInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject 
    private Instance<RememberMeIdentityStore> rememberMeIdentityStoreInstance;
    
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
        
        // If intercepting HttpAuthenticationMechanism#cleanSubject
        if (isImplementationOf(invocationContext.getMethod(), cleanSubjectMethod)) {
            cleanSubject(invocationContext,   
                (HttpServletRequest)invocationContext.getParameters()[0],
                (HttpServletResponse)invocationContext.getParameters()[1],
                (HttpMessageContext)invocationContext.getParameters()[2]);
        }
        
        return invocationContext.proceed();
    }
    
    private AuthStatus validateRequest(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        
        RememberMeIdentityStore rememberMeIdentityStore = rememberMeIdentityStoreInstance.get(); // TODO ADD CHECKS
        
        Cookie rememberMeCookie = getCookie(request, "JREMEMBERMEID");
        
        if (rememberMeCookie != null) {
            
            // There's a JREMEMBERMEID cookie, see if we can use it to authenticate
            
            CredentialValidationResult result = rememberMeIdentityStore.validate(
                new RememberMeCredential(rememberMeCookie.getValue())
            );
            
            if (result.getStatus() == VALID) {
                // The remember me store contained an authenticated identity associated with 
                // the given token, use it to authenticate with the container
                return httpMessageContext.notifyContainerAboutLogin(
                    result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                // The token appears to be no longer valid, or perhaps wasn't valid
                // to begin with. Remove the cookie.
                removeCookie(request, response, "JREMEMBERMEID");
            }
        }
        
        // Try to authenticate with the next interceptor or actual authentication mechanism
        AuthStatus authstatus = (AuthStatus) invocationContext.proceed();
        
        if (authstatus == SUCCESS && httpMessageContext.getCallerPrincipal() != null) {
            
            // Authentication succeeded;
            // Check if remember me is wanted by the caller and if so
            // store the authenticated identity in the remember me store
            // and send a cookie with a token that can be used
            // to retrieve this stored identity later
            
            RememberMe rememberMeAnnotation = getRememberMeFromIntercepted();
            
            Boolean isRememberMe = true;
            if (!isEmpty(rememberMeAnnotation.isRememberMeExpression())) {
                ELProcessor elProcessor = getElProcessor(invocationContext, httpMessageContext);
                
                isRememberMe = (Boolean) elProcessor.eval(rememberMeAnnotation.isRememberMeExpression());
            }
            
            if (isRememberMe) {
                String token = rememberMeIdentityStore.generateLoginToken(
                    httpMessageContext.getCallerPrincipal(),
                    httpMessageContext.getGroups()
                );
                
                saveCookie(request, response, "JREMEMBERMEID", token, rememberMeAnnotation.cookieMaxAgeSeconds());
            }
        }
        
        return authstatus;
    }
    
    private void cleanSubject(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
    
        RememberMeIdentityStore rememberMeIdentityStore = rememberMeIdentityStoreInstance.get(); // TODO ADD CHECKS
        
        Cookie rememberMeCookie = getCookie(request, "JREMEMBERMEID");
        
        if (rememberMeCookie != null) {
            
            // There's a JREMEMBERMEID cookie, remove the cookie
            removeCookie(request, response, "JREMEMBERMEID");
            
            // And remove the token (and with it the authenticated identity) from the store
            rememberMeIdentityStore.removeLoginToken(rememberMeCookie.getValue());
        }
        
    }
    
    private RememberMe getRememberMeFromIntercepted() {
        Optional<RememberMe> optionalRememberMe = getAnnotation(beanManager, interceptedBean.getBeanClass(), RememberMe.class);
        if (optionalRememberMe.isPresent()) {
            return optionalRememberMe.get();
        }
        
        throw new IllegalStateException("@RememberMe not present on " + interceptedBean.getBeanClass());
    }
    
    private ELProcessor getElProcessor(InvocationContext invocationContext, HttpMessageContext httpMessageContext) {
        ELProcessor elProcessor = new ELProcessor();
        
        elProcessor.getELManager().addELResolver(beanManager.getELResolver());
        elProcessor.defineBean("this", invocationContext.getTarget());
        elProcessor.defineBean("httpMessageContext", httpMessageContext);
        
        return elProcessor;
    }
 
}